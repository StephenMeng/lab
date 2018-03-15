package com.stephen.lab.controller.paper;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.hankcs.hanlp.HanLP;
import com.stephen.lab.constant.paper.TagType;
import com.stephen.lab.constant.semantic.ResultEnum;
import com.stephen.lab.dto.analysis.Token;
import com.stephen.lab.model.paper.Kiva;
import com.stephen.lab.model.paper.KivaSimple;
import com.stephen.lab.service.crawler.CrawlErrorService;
import com.stephen.lab.service.paper.KivaService;
import com.stephen.lab.util.*;
import com.stephen.lab.util.svm.SvmTrain;
import com.stephen.lab.util.svm.svm_predict;
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
import sun.rmi.runtime.Log;

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
    private static String stopWordPath = "C:\\Users\\stephen\\Desktop\\svm\\stopword.txt";

    static {
        try {
            stopwordList = getStopwords();
        } catch (IOException e) {
            e.printStackTrace();
        }
        props.put("annotators", "tokenize, ssplit, pos, lemma,  parse");    // 七种Annotators
        pipeline = new StanfordCoreNLP(props);
    }

    @Autowired
    private KivaService kivaService;
    @Autowired
    private CrawlErrorService crawlErrorService;
    private static final int PAGE_TOTAL = 71270;
    private static final int BUFFER_SIZE = 1024;
    private String allwordPath = "C:\\Users\\stephen\\Desktop\\svm\\allwords.txt";
    private String trainFilePath = "C:\\Users\\stephen\\Desktop\\svm\\train\\train-%s.txt";
    private String modelFilePath = "C:\\Users\\stephen\\Desktop\\svm\\model\\model-%s.txt";
    private String testFilePath = "C:\\Users\\stephen\\Desktop\\svm\\test\\testdata.txt";
    private String predictFilePath = "C:\\Users\\stephen\\Desktop\\svm\\predict\\predict-%s.txt";

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
                } catch (IOException e) {
                    crawlErrorService.addErrorItem(url, 14);
                }
            });
        }


        return Response.success("");
    }

    @RequestMapping("gen-textrank")
    public Response textrank(@RequestParam("num") int num) throws IOException {
        List<KivaSimple> kivaList = kivaService.selectAllSimple();
        kivaList.forEach(kivaSimple -> {
            String description = kivaSimple.getStandardDescription();
            description = description.replaceAll("#", " ");
            List<String> result = HanLP.extractKeyword(description, num);
            KivaSimple simple = new KivaSimple();
            simple.setId(kivaSimple.getId());
            KivaSimple s = kivaService.selectOne(simple);
            GenTag genTag = getGenTags(s.getGenTags());
            genTag.setTextRank(result);
            simple.setGenTags(JSONObject.toJSONString(genTag));
            kivaService.updateSimpleSelective(simple);
        });
        return Response.success(true);
    }

    private String removeHtmlTag(String description) {
        if (StringUtils.isNull(description)) {
            return "";
        }
        description = description.replaceAll("<.*?>", "");
        return description;
    }

    @RequestMapping("gen-tfidf")
    public Response genByTfIdf(int num) {
        List<KivaResult> kivaResults = getTFIDFResults();
        kivaResults.forEach(result -> {
            Collections.sort(result.getTokenList(), Comparator.comparing(Token::getWeight));
            Collections.reverse(result.getTokenList());
            List<Token> tokens = result.getTokenList().size() < num ? result.getTokenList() :
                    result.getTokenList().subList(0, num);
            List<String> r = Lists.transform(tokens, Token::getWord);
            KivaSimple simple = new KivaSimple();
            simple.setId(result.getKiva().getId());
            KivaSimple s = kivaService.selectOne(simple);
            GenTag genTag = getGenTags(s.getGenTags());
            genTag.setTfIdf(r);
            simple.setGenTags(JSONObject.toJSONString(genTag));
            kivaService.updateSimpleSelective(simple);
        });
        return Response.success("");
    }

    private GenTag getGenTags(String genTags) {
        GenTag genTag = JSONObject.parseObject(genTags, GenTag.class);
        if (genTag == null) {
            genTag = new GenTag();
        }
        return genTag;
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

    @RequestMapping("gen-knn")
    public Response tfidfKnn(@RequestParam("num") int num) throws IOException {

        List<KivaResult> kivaResults = getTFIDFResults();
        kivaResults.forEach(kivaResult -> {
            Collections.sort(kivaResult.getTokenList(), Comparator.comparing(Token::getWeight));
            List<KivaResult> selectResult = getKnnSimilarDoc(kivaResult, kivaResults, 5);
            List<String> sortedTags = getSortedTags(selectResult);
            updateDatabaseOfGenTag(kivaResult.getKiva().getId(), sortedTags, 3);
        });
        LogRecod.print("knn finished");
        return Response.success(true);
    }

    private void updateDatabaseOfGenTag(long id, List<String> newTags, int type) {
        KivaSimple simple = new KivaSimple();
        simple.setId(id);
        KivaSimple s = kivaService.selectOne(simple);
        GenTag genTag = getGenTags(s.getGenTags());
        switch (type) {
            case TagType.TFIDF:
                genTag.setTfIdf(newTags);
                break;
            case TagType.TEXTRANK:
                genTag.setTextRank(newTags);
                break;
            case TagType.KNN:
                genTag.setKnn(newTags);
                break;
            case TagType.SVM:
                genTag.setClassify(newTags);
                break;
            default:
        }
        simple.setGenTags(JSONObject.toJSONString(genTag));
        kivaService.updateSimpleSelective(simple);
    }

    private List<KivaResult> getTFIDFResults() {
        List<KivaResult> kivaResults = new ArrayList<>();
        List<KivaSimple> kivaList = getAllKivaSimpleResult();
        Map<String, Integer> yearWordMap = new HashMap<>();
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

    private List<KivaSimple> getAllKivaSimpleResult() {
        return kivaService.selectAllSimple();
    }

    private List<String> getSortedTags(List<KivaResult> selectResult) {
        List<String> result = new ArrayList<>();
        Map<String, Integer> map = new HashMap<>();
        selectResult.forEach(kivaResult -> {
            List<String> tags = Lists.newArrayList(Splitter.on("#").trimResults().split(kivaResult.getKiva().getTags()));
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
        if (map == null) {
            return new ArrayList<>();
        }
        Map<String, Integer> sortedMap = MapUtils.sortMapByValue(map, true);
        if (sortedMap == null) {
            return new ArrayList<>();
        }
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
//        List<Kiva> kivaList = kivaService.select(cond);
        List<Kiva> kivaList = kivaService.selectAll();

        kivaList.forEach(kiva -> {
            if (kiva != null) {
                KivaSimple simple = new KivaSimple(kiva);
                try {
                    String des = removeHtmlTag(kiva.getTranslatedDescription());
                    if (StringUtils.isNull(des)) {
                        des = removeHtmlTag(kiva.getOriginal_description());
                    }
                    List<String> ct = cutwords(des);
                    simple.setOriginalDescription(des);
                    simple.setStandardDescription(Joiner.on("#").join(ct));
                    simple.setTags(normalizeTags(kiva.getTags()));
                    kivaService.insertKivaSimple(simple);
                } catch (IOException e) {
                    e.printStackTrace();
                }
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
//                String word = token.get(CoreAnnotations.TextAnnotation.class);            // 获取分词
                String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);     // 获取词性标注
//                String ne = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);    // 获取命名实体识别结果
                String lemma = token.get(CoreAnnotations.LemmaAnnotation.class);          // 获取词形还原结果

                if (!stopwordList.contains(lemma) && !pos.startsWith("V") && !lemma.equals(",")) {
                    result.add(lemma);
                }
            }
        }
        return result;
    }


    @RequestMapping("svm/allwords")
    public Response allwords() {
        List<KivaSimple> kivaSimples = getAllKivaSimpleResult();
        int count = 0;
        Map<String, Integer> map = new HashMap<>();
        for (KivaSimple kivaSimple : kivaSimples) {
            try {
                List<String> stringList = splitString(kivaSimple.getStandardDescription());
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
        File file = new File(allwordPath);
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

    private List<String> getAllTags(int num) {
        Map<String, Integer> tagMaps = getAllTags();
        List<String> tags = new ArrayList<>();
        for (Map.Entry<String, Integer> map : tagMaps.entrySet()) {
            if (map.getValue() >= num) {
                tags.add(map.getKey());
            }
        }
        return tags;
    }

    @RequestMapping("svm/train/file/all")
    public Response trainSVMArgsAll() throws IOException {
        List<KivaSimple> kivaSimples = getAllKivaSimpleResult();
        List<KivaResult> kivaResults = getTFIDFResults();
        List<String> allTagWords = getAllTags(3);
        Map<String, Integer> map = getAllWordMap();
        allTagWords.forEach(tag -> {
            File tagFile = new File(String.format(trainFilePath, tag));
            kivaSimples.forEach(kivaSimple -> {
                outPutSVMStandardData(tag, map, tagFile, kivaSimple, kivaResults, false);
            });
        });

        return Response.success("");
    }

    private Map<String, Integer> getAllWordMap() {
        Map<String, Integer> result = new HashMap<>();
        try {
            List<String> stringList = Files.readLines(new File(allwordPath), Charsets.UTF_8);
            stringList.forEach(s -> {
                String[] sp = s.split("\t");
                result.put(sp[0], Integer.parseInt(sp[1]));
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    @RequestMapping("svm/train/file")
    public Response trainSVMFileSingle(String tag) throws IOException {
        List<KivaSimple> kivaSimples = getAllKivaSimpleResult();
        Map<String, Integer> map = getAllWordMap();
        List<KivaResult> kivaResults = getTFIDFResults();
        File tagFile = new File(String.format(trainFilePath, tag));
        kivaSimples.forEach(kivaSimple -> {
            outPutSVMStandardData(tag, map, tagFile, kivaSimple, kivaResults, false);
        });
        return Response.success(map);
    }

    private void outPutSVMStandardData(String tag, Map<String, Integer> map, File tagFile, KivaSimple kivaSimple, List<KivaResult> kivaResults, boolean asTestData) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            List<String> stringList = splitString(kivaSimple.getStandardDescription());
            KivaResult kivaResult = null;
            for (KivaResult k : kivaResults) {
                if (k.getKiva().getId().equals(kivaSimple.getId())) {
                    kivaResult = k;
                    break;
                }
            }
            List<Token> tokenList = kivaResult.getTokenList();
            stringList.forEach(s -> {
                if (map.get(s) != null) {
                    Token token = new Token();
                    token.setWord(s.toLowerCase());
                    int index = tokenList.indexOf(token);
                    if (index != -1) {
                        stringBuilder.append(" " + map.get(s) + ":");
                        double weight = tokenList.get(index).getWeight();
                        stringBuilder.append(weight);
                    }

                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (!StringUtils.isNull(stringBuilder.toString())) {
            if (asTestData) {
                try {
                    Files.write("-1" + stringBuilder.toString() + "\r\n", tagFile, Charsets.UTF_8);
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

    @RequestMapping("svm/train/args/all")
    public Response svmTrainArgsAll() throws IOException {
        List<String> tags = getAllTags(3);
        tags.forEach(tag -> {
            if (!StringUtils.isNull(tag)) {
                String[] arg = {String.format(trainFilePath, tag), //训练集
                        String.format(modelFilePath, tag)}; // 存放SVM训练模型
                try {
                    SvmTrain.main(arg);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        return Response.success("true");
    }

    @RequestMapping("svm/train/args")
    public Response svmTrain(@RequestParam("tag") String tag) throws IOException {
        String[] arg = {String.format(trainFilePath, tag), //训练集
                String.format(modelFilePath, tag)}; // 存放SVM训练模型
        SvmTrain.main(arg);
        return Response.success("true");
    }

    @RequestMapping("svm/generatorTestData")
    public Response generatorTestData(@RequestParam("id") long id) throws IOException {
        KivaSimple condition = new KivaSimple();
        condition.setId(id);
        List<KivaSimple> kivaSimples = kivaService.select(condition);
        List<KivaResult> kivaResults = getTFIDFResults();

        Map<String, Integer> map = getAllWordMap();
        File tagFile = new File(String.format(testFilePath));
        kivaSimples.forEach(kivaSimple -> {
            outPutSVMStandardData(null, map, tagFile, kivaSimple, kivaResults, true);
        });
        return Response.success("true");
    }

    @RequestMapping("svm/classify/all")
    public Response svmClassifyAll() throws IOException {
        List<KivaSimple> kivaSimples = getAllKivaSimpleResult();
        kivaSimples.forEach(kivaSimple -> {
            try {
                generatorTestData(kivaSimple.getId());
            } catch (IOException e) {
                e.printStackTrace();
            }
            List<String> tags = JSONObject.parseObject(kivaSimple.getGenTags(), GenTag.class).getKnn();
            List<String> svmTags = new ArrayList<>();
            tags.forEach(t -> {
                try {
                    svmClassify(t);
                    boolean yes = genSvmResult(t);
                    if (yes) {
                        svmTags.add(t);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            updateDatabaseOfGenTag(kivaSimple.getId(), svmTags, TagType.SVM);
        });
        return Response.success("");
    }

    private boolean genSvmResult(String t) {
        try {
            List<String> result = Files.readLines(new File(String.format(predictFilePath, t)), Charsets.UTF_8);
            if (result == null) {
                return false;
            }

            if (result.get(0).startsWith("0.0")) {
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @RequestMapping("svm/classify")
    public Response svmClassify(@RequestParam("tag") String tag) throws IOException {
        trainSVMFileSingle(tag);
        File file = new File(String.format(modelFilePath, tag));
        if (!file.exists()) {
            return Response.error(ResultEnum.FAIL_PARAM_WRONG);
        }
        String[] parg = {String.format(testFilePath), //测试数据
                String.format(modelFilePath, tag), // 调用训练模型
                String.format(predictFilePath, tag)}; //预测结果
        svm_predict.main(parg);  //调用

        return Response.success(genSvmResult(tag));
    }

    @RequestMapping("distance")
    public Response distance() {
        Kiva kiva = new Kiva();
        kiva.setId(85L);
        Kiva k = kivaService.selectOne(kiva);
        return Response.success("");
    }

    public static List<String> getStopwords() throws IOException {
        return Files.readLines(new File(stopWordPath), Charsets.UTF_8);
    }

    private Map<String, Integer> getAllTags() {
        List<KivaSimple> kivaSimples = kivaService.selectAllSimple();
        Map<String, Integer> result = new HashMap<>();
        kivaSimples.forEach(kivaSimple -> {
            List<String> tags = Lists.newArrayList(Splitter.on("#").trimResults().split(kivaSimple.getTags()));
            tags.forEach(tag -> {
                if (result.containsKey(tag)) {
                    result.put(tag, result.get(tag) + 1);

                } else {
                    result.put(tag, 1);
                }
            });
        });
        return result;
    }

    private double pValue(List<KivaSimple> simpleList, int tagType) throws IOException {
        int all = 0;
        int precise = 0;
        for (KivaSimple kivaSimple : simpleList) {
            GenTag genTag = JSONObject.parseObject(kivaSimple.getGenTags(), GenTag.class);
            List<String> originTags = splitString(kivaSimple.getTags());
            List<String> genTags = getTagsByTagType(tagType, genTag);
            precise += same(genTags, originTags);
            all += genTags.size();
        }
        return precise / (double) all;
    }

    @RequestMapping("fValue")
    public Response fvalue(int type) throws IOException {
        List<KivaSimple> kivaSimples = kivaService.selectAllSimple();
        double p = pValue(kivaSimples, type);
        double r = rValue(kivaSimples, type);
        double f = 2 * (p * r) / (p + r);
        Map<String, Object> result = new HashMap<>();
        result.put("p", p);
        result.put("r", r);
        result.put("f", f);
        return Response.success(result);

    }

    private double rValue(List<KivaSimple> simpleList, int tagType) throws IOException {
        int all = 0;
        int recall = 0;
        for (KivaSimple kivaSimple : simpleList) {
            GenTag genTag = JSONObject.parseObject(kivaSimple.getGenTags(), GenTag.class);
            List<String> originTags = splitString(kivaSimple.getTags());
            List<String> genTags = getTagsByTagType(tagType, genTag);
            recall += same(genTags, originTags);
            all += originTags.size();
        }
        return recall / (double) all;
    }

    private List<String> getTagsByTagType(int tagType, GenTag genTag) {
        List<String> genTags = null;
        switch (tagType) {
            case TagType.TFIDF:
                genTags = genTag.getTfIdf();
                break;
            case TagType.TEXTRANK:
                genTags = genTag.getTextRank();
                break;
            case TagType.KNN:
                genTags = genTag.getKnn();
                break;
            case TagType.SVM:
                genTags = genTag.getClassify();
                break;
            default:
                genTags = genTag.getTfIdf();

        }
        return genTags;
    }

    private int same(List<String> ls, List<String> lb) {
        if (ls == null || lb == null || ls.size() == 0 || lb.size() == 0) {
            return 0;
        }
        int count = 0;
        Iterator<String> it = ls.iterator();
        while (it.hasNext()) {
            if (lb.contains(it.next())) {
                count++;
            }
        }
        return count;
    }

}
