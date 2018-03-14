package com.stephen.lab.controller.paper;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.hankcs.hanlp.HanLP;
import com.stephen.lab.dto.analysis.Token;
import com.stephen.lab.model.paper.Kiva;
import com.stephen.lab.model.paper.KivaSimple;
import com.stephen.lab.service.crawler.CrawlErrorService;
import com.stephen.lab.service.paper.KivaService;
import com.stephen.lab.util.*;
import com.stephen.lab.util.svm.svm_predict;
import com.stephen.lab.util.svm.SvmTrain;
import com.sun.corba.se.impl.oa.toa.TOA;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import io.swagger.annotations.ApiOperation;
import io.swagger.models.auth.In;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.wltea.analyzer.cfg.Configuration;
import org.wltea.analyzer.cfg.DefaultConfig;
import org.wltea.analyzer.core.IKSegmenter;
import org.wltea.analyzer.core.Lexeme;

import java.io.*;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * @author Stephen
 */
@RestController
@RequestMapping("kiva")
public class KivaController {
    private static List<String> stopwordList;
    private static Properties props = new Properties();
    private static StanfordCoreNLP pipeline;    // 依次处理

    static {
        try {
            stopwordList = getStopwords();
        } catch (IOException e) {
            e.printStackTrace();
        }
        props.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");    // 七种Annotators
        pipeline = new StanfordCoreNLP(props);
    }

    @Autowired
    private KivaService kivaService;
    @Autowired
    private CrawlErrorService crawlErrorService;
    private static final int PAGE_TOTAL = 71270;
    private static final int BUFFER_SIZE = 1024;

    @ApiOperation(value = "解析loans", notes = "将数据解析到数据库")
    @RequestMapping(value = "init", method = RequestMethod.GET)
    public Response initKivaInfo() throws IOException {
        String filePath = "c:/users/stephen/desktop/paper/loans.json";
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(filePath))));
        int count = 0;
        char[] bytes = new char[1024];
        String result = "";
        while (reader.read(bytes, 0, BUFFER_SIZE) != -1) {
            String line = new String(bytes);
            result += line;
            result = parseText(result);
        }
        return Response.success(result);
    }

    private String parseText(String result) {
        int index = result.indexOf("]},");
        if (index != -1) {
            index = index + 2;
            String json = result.substring(1, index);
            LogRecod.print(json);
            Kiva kiva = JSONObject.parseObject(json, Kiva.class);
            result = result.substring(index);
            int nextIndex = result.indexOf("}]},");
            try {
                kivaService.insert(kiva);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (nextIndex != -1) {
                result = parseText(result);
            }
            return result;
        } else {
            return result;
        }
    }

    @ApiOperation(value = "下载loans", notes = "将数据解析到数据库")
    @RequestMapping(value = "doanload", method = RequestMethod.GET)
    public Response downloadKivaInfo() throws IOException {
        String baseUrl = "http://api.kivaws.org/v1/loans/search.json?page=";
        Executor executor = Executors.newFixedThreadPool(100);

        for (int i = 1; i < PAGE_TOTAL; i++) {
            int page = i;
            executor.execute(() -> {
                String url = baseUrl + page;
                HttpResponse response = null;
                try {
                    response = HttpUtils.httpGet(url);

                    String html = IOUtils.toString(response.getEntity().getContent(), Charsets.UTF_8);
                    JSONArray loanObj = JSONObject.parseObject(html).getJSONArray("loans");
                    List<Kiva> kivaList = loanObj.toJavaList(Kiva.class);

                    kivaList.forEach(kiva -> {
                        try {
                            kivaService.insert(kiva);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                    LogRecod.print(url);
                } catch (IOException e) {
                    crawlErrorService.addErrorItem(url, 14);
                }
            });
        }


        return Response.success("");
    }

    @RequestMapping("textrank")
    public Response textrank(@RequestParam("num") int num, @RequestParam("id") long id) throws IOException {
        KivaSimple condition = new KivaSimple();
        condition.setId(id);
        List<KivaSimple> kivaList = kivaService.select(condition);

        String description = kivaList.get(0).getStandardDescription();
        description = removeHtmlTag(description);
        return Response.success(HanLP.extractKeyword(description, num));
    }

    private String removeHtmlTag(String description) {
        description = description.replaceAll("<.*?>", "");
        return description;
    }

    @RequestMapping("tfidf")
    public Response tfidf(@RequestParam("id") long id, @RequestParam("num") int num) throws IOException {
        List<KivaResult> kivaResults = getTFIDFResults();
        KivaResult result = null;
        int size = kivaResults.size();
        for (int i = 0; i < size; i++) {
            if (kivaResults.get(i).getKiva().getId().equals(id)) {
                result = kivaResults.get(i);
                break;
            }
        }
        Collections.sort(result.getTokenList(), Comparator.comparing(Token::getWeight));
        Collections.reverse(result.getTokenList());
        List<Token> tokens = result.getTokenList().subList(0, num);
        List<String> r = Lists.transform(tokens, Token::getWord);
        return Response.success(r);
    }

    @RequestMapping("tfidf-knn")
    public Response tfidfKnn(@RequestParam("id") long id, @RequestParam("num") int num) throws IOException {

        List<KivaResult> kivaResults = getTFIDFResults();
        KivaResult kivaResult = kivaResults.get(0);
        Collections.sort(kivaResult.getTokenList(), Comparator.comparing(Token::getWeight));
        List<KivaResult> selectResult = getKnnSimilarDoc(kivaResult, kivaResults, 5);
        List<String> sortedTags = getSortedTags(selectResult);
        return Response.success(sortedTags);
    }

    private List<KivaResult> getTFIDFResults() {
        List<KivaResult> kivaResults = new ArrayList<>();
        List<KivaSimple> kivaList = kivaService.selectAllSimple();
        Map<String, Integer> yearWordMap = new HashMap<>();
        LogRecod.print(kivaList.size());
        long cur = System.currentTimeMillis();
        kivaList.forEach(kiva -> {
            String description = kiva.getStandardDescription();
            List<String> words = null;
            try {
                words = splitString(description);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Map<String, Integer> keywordsMap = parseKeywordMap(words);
            List<Token> tokens = new ArrayList<>();
            for (Map.Entry<String, Integer> kwm : keywordsMap.entrySet()) {
                Token token = new Token();
                token.setWord(kwm.getKey());
                token.setFreq(kwm.getValue());
                tokens.add(token);
                if (yearWordMap.containsKey(kwm.getKey())) {
                    yearWordMap.put(kwm.getKey(), yearWordMap.get(kwm.getKey()) + 1);

                } else {
                    yearWordMap.put(kwm.getKey(), 1);
                }

            }
            KivaResult kivaResult = new KivaResult(kiva);
            kivaResult.setTokenList(tokens);
            kivaResults.add(kivaResult);
        });


        LogRecod.print("计算关键词在年文档出现的频率" + (System.currentTimeMillis() - cur));
        for (KivaResult r : kivaResults) {
            for (Token t : r.getTokenList()) {
                int count = yearWordMap.get(t.getWord());
                double weight = Math.log(yearWordMap.size() / (count + 0.0000001)) * t.getFreq();
                t.setDocCount(count);
                t.setWeight(weight);
            }
        }
        return kivaResults;
    }

    private List<String> getSortedTags(List<KivaResult> selectResult) {
        List<String> result = new ArrayList<>();
        Map<String, Integer> map = new HashMap<>();
        selectResult.forEach(kivaResult -> {
            List<String> tags = Lists.newArrayList(Splitter.on(";").trimResults().split(kivaResult.getKiva().getTags()));
            if (tags != null && tags.size() > 0) {
                tags.forEach(tag -> {
                    if (!StringUtils.isNull(tag)) {
                        if (map.containsKey(tag)) {
                            map.put(tag, map.get(tag) + 1);
                        } else {
                            map.put(tag, 1);
                        }
                    }
                });
            }
        });
        Map<String, Integer> sortedMap = MapUtils.sortMapByValue(map, true);
        for (Map.Entry<String, Integer> m : sortedMap.entrySet()) {
            result.add(m.getKey());
        }
        return result;
    }


    private List<KivaResult> getKnnSimilarDoc(KivaResult kivaResult, List<KivaResult> toCompare, int n) {
        List<KivaResult> result = new ArrayList<>(n);
        double[] score = new double[n];
        for (int i = 0; i < score.length; i++) {
            score[i] = Double.MAX_VALUE;
        }
        toCompare.forEach(to -> {
            if (!to.getKiva().getId().equals(kivaResult.getKiva().getId())) {
                double distance = computeDistance(kivaResult.getTokenList(), to.getTokenList());
                to.setDistanceToOther(distance);
                int index = indexOfessThanComputed(distance, score);
                if (index != -1) {
                    if (result.size() <= index) {
                        result.add(to);
                    } else {
                        result.set(index, to);
                    }
                    score[index] = distance;
                }
            }
        });
        Collections.sort(result, Comparator.comparing(KivaResult::getDistanceToOther));
        return result;
    }

    private int indexOfessThanComputed(double distance, double[] score) {
        for (int i = 0; i < score.length; i++) {
            double item = score[i];
            if (item == -1 || distance < item) {
                return i;
            }
        }
        return -1;
    }

    private double computeDistance(List<Token> ka, List<Token> kb) {
        return DistanceUtils.cosDistance(ka, kb);
    }

    private Map<String, Integer> parseKeywordMap(List<String> words) {
        Map<String, Integer> map = new HashMap<>();
        words.forEach(word -> {
            if (map.containsKey(word)) {
                map.put(word, map.get(word) + 1);
            } else {
                map.put(word, 1);
            }
        });
        return map;
    }

    @RequestMapping("tranformToSimple")
    public Response tranformToSimple(long id) {
        Kiva cond = new Kiva();
        cond.setId(id);
        List<Kiva> kivaList = kivaService.selectAll();
//        List<Kiva> kivaList = kivaService.selectAll();

        kivaList.forEach(kiva -> {
            KivaSimple simple = new KivaSimple(kiva);
            try {
                String des = removeHtmlTag(kiva.getTranslatedDescription());
                if (StringUtils.isNull(des)) {
                    des = kiva.getOriginal_description();
                }
                List<String> ct = cutwords(des);
                simple.setOriginalDescription(des);
                simple.setStandardDescription(Joiner.on(" ").join(ct));
                simple.setTags(normalizeTags(kiva.getTags()));
                kivaService.insertKivaSimple(simple);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        return Response.success("");
    }

    private String normalizeTags(String tags) {
        StringBuilder result = new StringBuilder();
        String[] ts = tags.split(",");
        for (String t : ts) {
            if (t.contains("#")) {
                result.append(t.toLowerCase());
            }
        }
        return result.toString();
    }

    private List<String> splitString(String description) throws IOException {
        if (StringUtils.isNull(description)) {
            return new ArrayList<>();
        }
        return Arrays.asList(description.split("#"));
    }

    @RequestMapping("cutword")
    public Response cutwor(long id) throws IOException {
        Kiva condition = new Kiva();
        condition.setId(id);
        Kiva kiva = kivaService.selectOne(condition);
        return Response.success(cutwords(kiva.getTranslatedDescription()));
    }

    private List<String> cutwords(String description) throws IOException {
        if (StringUtils.isNull(description)) {
            return new ArrayList<>();
        }
        List<String> result = new ArrayList<>();
        Annotation document = new Annotation(description);    // 利用text创建一个空的Annotation
        pipeline.annotate(document);                   // 对text执行所有的Annotators（七种）

        // 下面的sentences 中包含了所有分析结果，遍历即可获知结果。
        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);

        for (CoreMap sentence : sentences) {
            for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                String word = token.get(CoreAnnotations.TextAnnotation.class);            // 获取分词
                String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);     // 获取词性标注
                String ne = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);    // 获取命名实体识别结果
                String lemma = token.get(CoreAnnotations.LemmaAnnotation.class);          // 获取词形还原结果
                System.out.println(word + "\t" + pos + "\t" + lemma + "\t" + ne);

                if (!stopwordList.contains(lemma) && !pos.startsWith("V")) {
                    result.add(lemma + "#");
                }
            }
        }
        return result;
    }


    @RequestMapping("svm/train/file")
    public Response trainSVM() {
        List<KivaSimple> kivaSimples = kivaService.selectAllSimple();
        List<String> allWords = new ArrayList<>();
        int count = 0;
        Map<String, Integer> map = new HashMap<>();
        for (KivaSimple kivaSimple : kivaSimples) {
            try {
                List<String> stringList = splitString(kivaSimple.getOriginalDescription());
                for (String s : stringList) {
                    map.put(s, 1);
                    if (map.size() > count) {
                        count++;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        File file = new File("C:\\Users\\stephen\\Desktop\\allwords.txt");
        int index = 0;
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            try {
                Files.append(entry.getKey() + "\t" + index + "\r\n", file, Charsets.UTF_8);
                index++;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return Response.success("");
    }

    @RequestMapping("svm/train/args")
    public Response trainSVMArgs(String tag) throws IOException {
        List<KivaSimple> kivaSimples = kivaService.selectAllSimple();
        Map<String, Integer> map = getAllTagsMap();
        File tagFile = new File("C:\\Users\\stephen\\Desktop\\svm\\" + tag + ".txt");
        kivaSimples.forEach(kivaSimple -> {
            outPutSVMStandardData(tag, map, tagFile, kivaSimple, false);
        });
        return Response.success(map);
    }

    private void outPutSVMStandardData(String tag, Map<String, Integer> map, File tagFile, KivaSimple kivaSimple, boolean asTestData) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            List<String> stringList = splitString(kivaSimple.getStandardDescription());
            stringList.forEach(s -> {
                if (map.get(s) != null) {
                    stringBuilder.append(" " + map.get(s) + ":1");
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (!StringUtils.isNull(stringBuilder.toString())) {
            if (asTestData) {
                try {
                    Files.append("-1" + stringBuilder.toString() + "\r\n", tagFile, Charsets.UTF_8);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (kivaSimple.getTags().contains(tag)) {
                try {
                    Files.append("1" + stringBuilder.toString() + "\r\n", tagFile, Charsets.UTF_8);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    Files.append("0" + stringBuilder.toString() + "\r\n", tagFile, Charsets.UTF_8);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private Map<String, Integer> getAllTagsMap() throws IOException {
        Map<String, Integer> map = new HashMap<>();
        File file = new File("C:\\Users\\stephen\\Desktop\\allwords.txt");
        List<String> strings = Files.readLines(file, Charsets.UTF_8);
        for (String s : strings) {
            String[] term = s.split("\t");
            try {
                map.put(term[0], Integer.parseInt(term[1]));
            } catch (Exception e) {
            }
        }
        return map;
    }

    @RequestMapping("svm/test")
    public Response svm() throws IOException {
        String[] arg = {"C:\\Users\\stephen\\Desktop\\svm\\bussiness.txt", //训练集
                "C:\\Users\\stephen\\Desktop\\svm\\bussiness-model.txt"}; // 存放SVM训练模型

        String[] parg = {"C:\\Users\\stephen\\Desktop\\svm\\testdata.txt", //测试数据
                "C:\\Users\\stephen\\Desktop\\svm\\bussiness-model.txt", // 调用训练模型
                "C:\\Users\\stephen\\Desktop\\svm\\predict.txt"}; //预测结果
        System.out.println("........SVM运行开始..........");
//        long start = System.currentTimeMillis();

        //创建一个预测或者分类的对象
        SvmTrain t = new SvmTrain();
        t.main(arg);   //调用
        svm_predict.main(parg);  //调用
        return Response.success("");
    }

    @RequestMapping("svm/train")
    public Response svmTrain(@RequestParam("tag") String tag) throws IOException {
        String[] arg = {"C:\\Users\\stephen\\Desktop\\svm\\" + tag + ".txt", //训练集
                "C:\\Users\\stephen\\Desktop\\svm\\" + tag + "-model.txt"}; // 存放SVM训练模型
        SvmTrain.main(arg);
        return Response.success("true");
    }

    @RequestMapping("svm/generatorTestData")
    public Response generatorTestData(@RequestParam("id") long id, @RequestParam("tag") String tag) throws IOException {
        KivaSimple condition = new KivaSimple();
        condition.setId(id);
        List<KivaSimple> kivaSimples = kivaService.select(condition);
        Map<String, Integer> map = getAllTagsMap();
        File tagFile = new File("C:\\Users\\stephen\\Desktop\\svm\\" + tag + "-testdata.txt");
        kivaSimples.forEach(kivaSimple -> {
            outPutSVMStandardData(tag, map, tagFile, kivaSimple, true);
        });
        return Response.success("true");
    }

    @RequestMapping("svm/classify")
    public Response svmClassify(@RequestParam("tag") String tag) throws IOException {
        trainSVMArgs(tag);
        String[] parg = {"C:\\Users\\stephen\\Desktop\\svm\\" + tag + "-testdata.txt", //测试数据
                "C:\\Users\\stephen\\Desktop\\svm\\" + tag + "-model.txt", // 调用训练模型
                "C:\\Users\\stephen\\Desktop\\svm\\" + tag + "-predict.txt"}; //预测结果
        svm_predict.main(parg);  //调用
        return Response.success("");
    }

    @RequestMapping("distance")
    public Response distance() {
        Kiva kiva = new Kiva();
        kiva.setId(85L);
        Kiva k = kivaService.selectOne(kiva);
        return Response.success("");
    }

    public static List<String> getStopwords() throws IOException {
        return Files.readLines(new File("C:\\Users\\stephen\\Desktop\\svm\\stopword.txt"), Charsets.UTF_8);
    }
}
