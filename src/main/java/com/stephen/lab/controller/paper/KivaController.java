package com.stephen.lab.controller.paper;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.stephen.lab.constant.paper.TagType;
import com.stephen.lab.constant.semantic.ResultEnum;
import com.stephen.lab.dto.analysis.FreqToken;
import com.stephen.lab.dto.analysis.Token;
import com.stephen.lab.model.paper.*;
import com.stephen.lab.service.crawler.CrawlErrorService;
import com.stephen.lab.service.paper.KivaService;
import com.stephen.lab.util.*;
import com.stephen.lab.util.nlp.ClusterUtils;
import com.stephen.lab.util.nlp.KeywordExtractUtils;
import com.stephen.lab.util.nlp.NLPIRUtil;
import com.stephen.lab.util.nlp.lda.sample.conf.ConstantConfig;
import com.stephen.lab.util.nlp.lda.sample.main.Documents;
import com.stephen.lab.util.nlp.lda.sample.main.LdaGibbsSampling;
import com.stephen.lab.util.nlp.lda.sample.main.LdaModel;
import com.stephen.lab.util.svm.SvmTrain;
import com.stephen.lab.util.svm.svm_predict;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static com.stephen.lab.util.nlp.lda.sample.main.LdaGibbsSampling.getParametersFromFile;

/**
 * @author Stephen
 */
@RestController
@RequestMapping("kiva")
public class KivaController {
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
    public Response genTagByTextRank(@RequestParam("num") int num) throws IOException {
        List<KivaSimple> kivaList = kivaService.selectAllSimple();
        kivaList.forEach(kivaSimple -> {
            String description = kivaSimple.getStandardDescription();
            description = description.replaceAll("#", " ");
            List<Token> result = KeywordExtractUtils.textrank(description, num);
            updateDatabaseOfGenTag(kivaSimple.getId(), result, TagType.TEXTRANK);
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
    public Response genTagByTfIdf(int num) {
        List<KivaResult> kivaResults = getTFIDFResults(null);
        kivaResults.forEach(result -> {
            Collections.sort(result.getTokenList(), Comparator.comparing(Token::getWeight));
            Collections.reverse(result.getTokenList());
            List<FreqToken> freqTokens = (result.getTokenList().size() < num ? result.getTokenList() :
                    result.getTokenList().subList(0, num));
            List<Token> tokens = getTokenListFromFreqTokens(freqTokens);
            updateDatabaseOfGenTag(result.getKiva().getId(), tokens, TagType.TFIDF);
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
        List<KivaResult> kivaResults = getTFIDFResults(null);
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
        List<FreqToken> tokens = result.getTokenList().subList(0, num);
        List<String> r = Lists.transform(tokens, Token::getWord);
        return Response.success(r);
    }

    @RequestMapping("gen-knn")
    public Response genTagByKnn(@RequestParam("num") int num) throws IOException {

        List<KivaResult> kivaResults = getTFIDFResults(null);
        kivaResults.forEach(kivaResult -> {
            Collections.sort(kivaResult.getTokenList(), Comparator.comparing(Token::getWeight));
            List<KivaResult> selectResult = getKnnSimilarDoc(kivaResult, kivaResults, 5);
            List<Token> sortedTags = getSortedTags(selectResult);
            updateDatabaseOfGenTag(kivaResult.getKiva().getId(), sortedTags, TagType.KNN);
        });
        LogRecod.print("knn finished");
        return Response.success(true);
    }

    private void updateDatabaseOfGenTag(long id, List<Token> newTags, int type) {
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
                genTag.setSvm(newTags);
                break;
            case TagType.LDA:
                genTag.setLda(newTags);
                break;
            default:
        }
        simple.setGenTags(JSONObject.toJSONString(genTag));
        kivaService.updateSimpleSelective(simple);
    }

    private List<KivaResult> getTFIDFResults(String newText) {
        List<KivaResult> kivaResults = new ArrayList<>();
        List<KivaSimple> kivaList = getAllKivaSimpleResult();
        if (!StringUtils.isNull(newText)) {
            KivaSimple simple = new KivaSimple();
            simple.setId(-1L);
            simple.setOriginalDescription(newText);
            try {
                simple.setStandardDescription(Joiner.on("#").join(cutwords(newText)));
            } catch (IOException e) {
                e.printStackTrace();
            }
            kivaList.add(simple);
        }
        Map<String, Integer> yearWordMap = new HashMap<>();
        kivaList.forEach(kiva -> {
            String description = kiva.getStandardDescription();
            List<String> words = null;
            try {
                words = splitString(description);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Map<String, Integer> keywordsMap = parseKeywordMap(words);
            List<FreqToken> tokens = new ArrayList<>();
            for (Map.Entry<String, Integer> kwm : keywordsMap.entrySet()) {
                FreqToken token = new FreqToken();
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
            for (FreqToken t : r.getTokenList()) {
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

    private List<Token> getSortedTags(List<KivaResult> selectResult) {
        List<Token> result = new ArrayList<>();
        Map<String, Integer> map = new HashMap<>();
        selectResult.forEach(kivaResult -> {
            List<String> tags = NLPIRUtil.split(kivaResult.getKiva().getTags(), TagType.TAG_SPLITTER);
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
            Token token = new Token();
            token.setWord(m.getKey());
            token.setWeight(m.getValue());
            result.add(token);
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
                List<Token> tokens = getTokenListFromFreqTokens(kivaResult.getTokenList());
                List<Token> toTokens = getTokenListFromFreqTokens(to.getTokenList());
                double distance = computeDistance(tokens, toTokens);
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

    private List<KivaResult> getLDASimilarDoc(KivaResult kivaResult, List<KivaSimple> toCompare,
                                              List<Double> srcScore,
                                              List<List<Double>> toCompareScores, int n) {
        List<KivaResult> result = new ArrayList<>(n);
        double[] score = new double[n];
        for (int i = 0; i < score.length; i++) {
            score[i] = Double.MAX_VALUE;
        }
        for (int i = 0; i < toCompare.size(); i++) {
            KivaSimple kivaSimple = toCompare.get(i);
            KivaResult kr = new KivaResult(kivaSimple);
            if (!kivaSimple.getId().equals(kivaResult.getKiva().getId())) {
                List<Double> toCompareScore = toCompareScores.get(i);
                double distance = DistanceUtils.cosNumricDistance(toCompareScore, srcScore);
                int index = indexOfessThanComputed(distance, score);
                if (index != -1) {
                    if (result.size() <= index) {
                        result.add(kr);
                    } else {
                        result.set(index, kr);
                    }
                    kr.setDistanceToOther(distance);
                    score[index] = distance;
                }
            }
        }
        Collections.sort(result, Comparator.comparing(KivaResult::getDistanceToOther));
        return result;
    }

    private List<Token> getTokenListFromFreqTokens(List<? extends Token> tokenList) {
        return Lists.transform(tokenList, Token::new);
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
        cond.setSector("Education");
        List<Kiva> kivaList = kivaService.select(cond);

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
        List<String> result = NLPIRUtil.cutwords(description);
        result = NLPIRUtil.removeStopwords(result);
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
        List<KivaResult> kivaResults = getNormalingTFIDFResults(null);
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

    private List<KivaResult> getNormalingTFIDFResults(String text) {
        List<KivaResult> kivaResults = getTFIDFResults(text);
        double max = -1;
        double min = Double.MAX_VALUE;
        for (int i = 0; i < kivaResults.size(); i++) {
            List<FreqToken> tokenLit = kivaResults.get(i).getTokenList();
            for (int j = 0; j < tokenLit.size(); j++) {
                double weight = tokenLit.get(j).getWeight();
                if (weight > max) {
                    max = weight;
                }
                if (weight < min) {
                    min = weight;
                }
            }
        }
        for (int i = 0; i < kivaResults.size(); i++) {
            List<FreqToken> tokenLit = kivaResults.get(i).getTokenList();
            for (int j = 0; j < tokenLit.size(); j++) {
                FreqToken token = tokenLit.get(j);
                if (token.getWeight() > 0) {
                    token.setWeight(token.getWeight() / max);
                }
                if (token.getWeight() < 0) {
                    token.setWeight(token.getWeight() / min);
                }
            }
        }
        return kivaResults;
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

    //    @RequestMapping("svm/train/file")
    public Response trainSVMFileSingle(String tag,
                                       List<KivaResult> kivaResults,
                                       Map<String, Integer> map) throws IOException {
        List<KivaSimple> kivaSimples = getAllKivaSimpleResult();
        if (map == null) {
            map = getAllWordMap();
        }
        if (kivaResults == null) {
            kivaResults = getNormalingTFIDFResults(null);
        }
        File tagFile = new File(String.format(trainFilePath, tag));
        tagFile.delete();
        for (KivaSimple kivaSimple : kivaSimples) {
            outPutSVMStandardData(tag, map, tagFile, kivaSimple, kivaResults, false);
        }
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
            List<Token> tokenList = getTokenListFromFreqTokens(kivaResult.getTokenList());
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

    @RequestMapping("svm/train/args")
    public Response svmTrain(@RequestParam("tag") String tag, List<KivaResult> kivaResults, Map<String, Integer> map) throws IOException {
        trainSVMFileSingle(tag, kivaResults, map);
        String[] arg = getTrainModelPath(tag);
        SvmTrain.main(arg);
        return Response.success("true");
    }

    @RequestMapping("svm/train/args/all")
    public Response svmTrainArgsAll() throws IOException {
        List<String> tags = getAllTags(2);
        List<KivaResult> kivaResults = getNormalingTFIDFResults(null);
        Map<String, Integer> map = getAllWordMap();
        tags.forEach(tag -> {
            if (!StringUtils.isNull(tag)) {
                try {
                    svmTrain(tag, kivaResults, map);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        return Response.success("true");
    }

    private String[] getTrainModelPath(@RequestParam("tag") String tag) {
        return new String[]{
                "-g", "2.0", "-c", "32", "-t", "2", "-m", "500.0", "-h", "0",
                String.format(trainFilePath, tag), //训练集
                String.format(modelFilePath, tag)};
    }

    @RequestMapping("svm/generatorTestData")
    public Response generatorTestData(@RequestParam("id") long id,
                                      @RequestParam(value = "kr", required = false) List<KivaResult> kivaResults,
                                      @RequestParam(value = "map", required = false) Map<String, Integer> map) throws IOException {
        KivaSimple kivaSimple = kivaService.selectSimpleById(id);
        if (kivaResults == null) {
            kivaResults = getNormalingTFIDFResults(null);
        }
        if (map == null) {
            map = getAllWordMap();
        }
        File tagFile = new File(String.format(testFilePath));
        outPutSVMStandardData(null, map, tagFile, kivaSimple, kivaResults, true);
        return Response.success("true");
    }

    @RequestMapping("svm/classify/all")
    public Response svmClassifyAll() throws IOException {
        List<KivaSimple> kivaSimples = getAllKivaSimpleResult();
//        kivaSimples = kivaSimples.subList(0, 1);
//        LogRecod.print(kivaSimples.get(0).getId());
        List<KivaResult> kivaResults = getNormalingTFIDFResults(null);
        Map<String, Integer> map = getAllWordMap();
        kivaSimples.forEach(kivaSimple -> {
            try {
                generatorTestData(kivaSimple.getId(), kivaResults, map);
            } catch (IOException e) {
                e.printStackTrace();
            }
            List<Token> tags = JSONObject.parseObject(kivaSimple.getGenTags(), GenTag.class).getKnn();
            List<Token> svmTags = new ArrayList<>();
            tags.forEach(t -> {
                try {
                    svmClassify(t.getWord(), kivaResults, map);
                    boolean yes = genSvmResult(t.getWord());
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
        return true;
    }

    @RequestMapping("svm/classify")
    public Response svmClassify(@RequestParam("tag") String tag, List<KivaResult> kivaResults, Map<String, Integer> map) throws IOException {
        svmTrain(tag, kivaResults, map);
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
            List<Token> originalTokens = Lists.transform(originTags, Token::new);
            List<Token> genTags = getTagsByTagType(tagType, genTag);
            precise += same(genTags, originalTokens);
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
            List<Token> originalTokens = Lists.transform(originTags, Token::new);
            List<Token> genTags = getTagsByTagType(tagType, genTag);
            recall += same(genTags, originalTokens);
            all += originTags.size();
        }
        return recall / (double) all;
    }

    private List<Token> getTagsByTagType(int tagType, GenTag genTag) {
        List<? extends Token> genFreqTags = null;
        switch (tagType) {
            case TagType.TFIDF:
                genFreqTags = genTag.getTfIdf();
                break;
            case TagType.TEXTRANK:
                genFreqTags = genTag.getTextRank();
                break;
            case TagType.KNN:
                genFreqTags = genTag.getKnn();
                break;
            case TagType.SVM:
                genFreqTags = genTag.getSvm();
                break;
            case TagType.LDA:
                genFreqTags = genTag.getLda();
                break;
            default:
                genFreqTags = genTag.getTfIdf();
        }
        return getTokenListFromFreqTokens(genFreqTags);
    }

    private int same(List<Token> ls, List<Token> lb) {
        if (ls == null || lb == null || ls.size() == 0 || lb.size() == 0) {
            return 0;
        }
        int count = 0;
        Iterator<Token> it = ls.iterator();
        while (it.hasNext()) {
            if (lb.contains(it.next())) {
                count++;
            }
        }
        return count;
    }

    @RequestMapping("gen-tag")
    public Response genTag(int type, int num) throws IOException {
        Response response = Response.error(ResultEnum.FAIL_PARAM_WRONG);
        switch (type) {
            case TagType.TFIDF:
                response = genTagByTfIdf(num);
                break;
            case TagType.TEXTRANK:
                response = genTagByTextRank(num);
                break;
            case TagType.KNN:
                response = genTagByKnn(num);
                break;
            case TagType.LDA:
                response = genTagByLDA(num);
                break;
            case TagType.SVM:
                break;
        }
        return response;
    }

    @RequestMapping("gen-tag-single")
    public Response genTagSingle(String text, int type, int startNum, int endNum) throws IOException {
        text = "Sokhoeurn is 36 years old and has three children, two under-age for schooling and one child in school.\n" +
                "\n" +
                "Her main sources of income are from rice farming, cassava farming, and a fish selling business. She has been involved in her current business for 16 years. With this new loan, she is in her fourth loan cycle with VisionFund (Kiva’s partner). She has paid back her past three loans with success and it has enabled her to support her business timely.\n" +
                "\n" +
                "Now she is seeking 4,000,000 KHR in order to renovate her house. She hopes that she can fix her house timely so that she does not need to worry about the upcoming rainy season.";
        List<Token> result = null;
        switch (type) {
            case TagType.TFIDF:
                result = genTagForNewTextByTfIdf(text, endNum);
                break;
            case TagType.TEXTRANK:
                result = genTagForNewTextByTextrank(text, endNum);
                break;
            case TagType.KNN:
                result = genTagForNewTextByKnn(text, endNum);
                break;
            case TagType.LDA:
                result = genTagForNewTextByLda(text, endNum);
                break;
            case TagType.SVM:
                break;
        }
        SortedClusterResult clusterResult = getSortedClusterResult(startNum, endNum, result);
        int bp = clusterResult.getBreakPoint();
        Map<String, Object> r = new HashMap<>();
        r.put("statistics", clusterResult);
        r.put("list", result.subList(0, bp + startNum));
        r.put("original_data", result);
        return Response.success(r);
    }

    private List<Token> genTagForNewTextByLda(String text, int endNum) {
        List<KivaSimple> kivaSimples = getAllKivaSimpleResult();
        KivaSimple kivaSimple = new KivaSimple();
        kivaSimple.setId(-1L);
        kivaSimple.setOriginalDescription(text);
        kivaSimple.setStandardDescription(
                Joiner.on(TagType.TAG_SPLITTER).join(NLPIRUtil.removeStopwords(NLPIRUtil.cutwords(text))));
        kivaSimples.add(kivaSimple);

        List<List<Double>> docThetas = getLdaScores(kivaSimples);
        docThetas = docThetas.subList(0, docThetas.size() - 1);
        KivaResult kivaResult = new KivaResult(kivaSimple);
        List<KivaResult> selectResult = getLDASimilarDoc(kivaResult, kivaSimples,
                docThetas.get(docThetas.size() - 1), docThetas, endNum);
        List<Token> sortedTags = getSortedTags(selectResult);
        return sortedTags;
    }

    private List<Token> genTagForNewTextByKnn(String text, int endNum) {
        List<KivaResult> kivaResults = getTFIDFResults(text);
        for (KivaResult kivaResult : kivaResults) {
            if (kivaResult.getKiva().getId() == -1L) {
                Collections.sort(kivaResult.getTokenList(), Comparator.comparing(Token::getWeight));
                List<KivaResult> selectResult = getKnnSimilarDoc(kivaResult, kivaResults, endNum);
                List<Token> sortedTags = getSortedTags(selectResult);
                return sortedTags;
            }
        }
        return new ArrayList<>();
    }

    private List<Token> genTagForNewTextByTextrank(String text, int endNum) {
        try {
            return KeywordExtractUtils.textrank(Joiner.on(" ").join(cutwords(text)), endNum);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    private List<Token> genTagForNewTextByTfIdf(String text, int num) {
        if (StringUtils.isNull(text)) {
            return new ArrayList<>();
        }
        List<KivaResult> kivaResults = getTFIDFResults(text);
        for (KivaResult kivaResult : kivaResults) {
            if (kivaResult.getKiva().getId() == -1L) {
                Collections.sort(kivaResult.getTokenList(), Comparator.comparing(Token::getWeight));
                Collections.reverse(kivaResult.getTokenList());
                List<FreqToken> freqTokens = (kivaResult.getTokenList().size() < num ? kivaResult.getTokenList() :
                        kivaResult.getTokenList().subList(0, num));
                List<Token> tokens = getTokenListFromFreqTokens(freqTokens);
                return tokens;
            }
        }
        return new ArrayList<>();
    }

    @RequestMapping("gen-breakpoint")
    public Response genBreakPoint(long id, int type, int startNum, int endNum) throws IOException {
        KivaSimple kivaSimple = kivaService.selectSimpleById(id);
        GenTag genTag = JSONObject.parseObject(kivaSimple.getGenTags(), GenTag.class);
        List<Token> list = null;
        switch (type) {
            case TagType.TFIDF:
                list = genTag.getTfIdf();
                break;
            case TagType.TEXTRANK:
                list = genTag.getTextRank();
                break;
            case TagType.KNN:
                list = genTag.getKnn();
                break;
            case TagType.SVM:
                list = genTag.getSvm();
                break;
        }
        SortedClusterResult result = getSortedClusterResult(startNum, endNum, list);
        int bp = result.getBreakPoint();
        Map<String, Object> r = new HashMap<>();
        r.put("statistics", result);
        r.put("list", list.subList(0, bp + startNum));
        return Response.success(r);
    }

    private SortedClusterResult getSortedClusterResult(int startNum, int endNum, List<Token> list) {
        List<Token> sublist = null;
        if (list.size() < startNum - 1) {
            sublist = list;
        } else if (list.size() < endNum + 1) {
            sublist = list.subList(startNum < 1 ? 0 : startNum - 1, list.size() - 1);
        } else {
            sublist = list.subList(startNum < 1 ? 0 : startNum - 1, endNum + 1);
        }
        return ClusterUtils.sortedCluster(sublist);
    }

    @RequestMapping("gen-lda")
    public Response genTagByLDA(int num) {
        List<KivaSimple> kivaList = kivaService.selectAllSimple();

        List<List<Double>> docThetas = getLdaScores(kivaList);
        for (int i = 0; i < kivaList.size(); i++) {
            KivaSimple kivaSimple = kivaList.get(i);
            List<Double> score = docThetas.get(i);
            KivaResult kivaResult = new KivaResult(kivaSimple);
            List<KivaResult> selectResult = getLDASimilarDoc(kivaResult, kivaList, score, docThetas, num);
            List<Token> sortedTags = getSortedTags(selectResult);
            updateDatabaseOfGenTag(kivaResult.getKiva().getId(), sortedTags, TagType.LDA);
        }
        return Response.success(docThetas);
    }

    private List<List<Double>> getLdaScores(List<KivaSimple> kivaList) {
        String parameterFile = ConstantConfig.LDAPARAMETERFILE;
        LdaGibbsSampling.modelparameters ldaparameters = new LdaGibbsSampling.modelparameters();
        getParametersFromFile(ldaparameters, parameterFile);
        List<String> contents = Lists.transform(kivaList, KivaSimple::getStandardDescription);
        Documents docSet = new Documents();
        docSet.readDocs(contents);
        System.out.println("wordMap size " + docSet.termToIndexMap.size());
        LdaModel model = new LdaModel(ldaparameters);
        System.out.println("1 Initialize the model ...");
        model.initializeModel(docSet);
        System.out.println("2 Learning and Saving the model ...");
        try {
            model.inferenceModel(docSet);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("3 Output the final model ...");
        return getThetaResultLists(ldaparameters, docSet, model);
    }

    private List<List<Double>> getThetaResultLists(LdaGibbsSampling.modelparameters ldaparameters, Documents docSet, LdaModel model) {
        List<List<Double>> docThetas = new ArrayList<>();
        double[][] theta = model.getTheta(ldaparameters.iteration, docSet);
        int m = model.getM();
        int k = model.getK();
        for (int i = 0; i < m; i++) {
            List<Double> item = new ArrayList<>();
            for (int j = 0; j < k; j++) {
                item.add(theta[i][j]);
            }
            docThetas.add(item);
        }
        return docThetas;
    }

    @RequestMapping("tag-num")
    public Response tagNum() {
        List<KivaSimple> simples = getAllKivaSimpleResult();
        Map<Integer, Integer> map = new HashMap<>();
        simples.forEach(simple -> {
            List<String> tags = NLPIRUtil.split(simple.getTags(), TagType.TAG_SPLITTER);
            LogRecod.print(tags);
            LogRecod.print(tags.size());

            if (map.containsKey(tags.size())) {
                map.put(tags.size(), map.get(tags.size()) + 1);
            } else {
                map.put(tags.size(), 1);
            }
        });
        return Response.success(map);
    }
}
