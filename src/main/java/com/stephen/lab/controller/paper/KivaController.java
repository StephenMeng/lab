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
import com.stephen.lab.util.nlp.classify.SVMUtil;
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
import java.util.stream.Collectors;

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
    private static final int TRAIN_DOC_NUM = 500;
    private String allwordPath = "C:\\Users\\stephen\\Desktop\\svm\\allwords.txt";
    private String trainFilePath = "C:\\Users\\stephen\\Desktop\\svm\\train\\train-%s.txt";
    private String modelFilePath = "C:\\Users\\stephen\\Desktop\\svm\\model\\model-%s.txt";
    private String testFilePath = "C:\\Users\\stephen\\Desktop\\svm\\test\\testdata.txt";
    private static final int TAG_FREQ = 1;
    private int F_VALUE_NUM = 6;

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
        List<KivaSimple> kivaList = kivaService.selectAllSimple(0);
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
        List<KivaResult> kivaResults = getTFIDFResults(null, 0);
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
        List<KivaResult> kivaResults = getTFIDFResults(null, 0);
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

        List<KivaResult> kivaResults = getTFIDFResults(null, 0);
        kivaResults.forEach(kivaResult -> {
            Collections.sort(kivaResult.getTokenList(), Comparator.comparing(Token::getWeight));
            List<KivaResult> selectResult = getKnnSimilarDoc(kivaResult, kivaResults, num);
            List<Token> sortedTags = getSortedTags(selectResult);
            sortedTags = sublist(sortedTags, num);
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
            case TagType.LDA:
                genTag.setLda(newTags);
                break;
            case TagType.SVM_KNN:
                genTag.setSvmKnn(newTags);
                break;
            case TagType.SVM_LDA:
                genTag.setSvmLda(newTags);
                break;
            case TagType.FILTER_KNN:
                genTag.setFilterKnn(newTags);
                break;
            case TagType.FILTER_LDA:
                genTag.setFilterLda(newTags);
                break;
            default:
        }
        simple.setGenTags(JSONObject.toJSONString(genTag));
        kivaService.updateSimpleSelective(simple);
    }

    private List<KivaResult> getTFIDFResults(String newText, int docNum) {
        LogRecod.print("tfidf 开始计算");
        List<KivaResult> kivaResults = new ArrayList<>();
        List<KivaSimple> kivaList = getAllKivaSimpleResult(docNum);
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
        LogRecod.print("tfidf 词汇频次计算完毕");

        for (KivaResult r : kivaResults) {
            for (FreqToken t : r.getTokenList()) {
                int count = yearWordMap.get(t.getWord());
                double weight = Math.log(yearWordMap.size() / (count + 0.0000001)) * t.getFreq();
                t.setDocCount(count);
                t.setWeight(weight);
            }
        }
        LogRecod.print("tfidf log值计算完毕");
        LogRecod.print("tfidf 计算完毕");
        return kivaResults;
    }

    private List<KivaSimple> getAllKivaSimpleResult(int docNum) {
        return kivaService.selectAllSimple(docNum);
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
    public Response tranformToSimple(int num) {

        List<Kiva> kivaList = kivaService.selectAll(num);

        kivaList = kivaList.stream().filter(kiva -> kiva.getTags().contains(TagType.TAG_SPLITTER)).collect(Collectors.toList());
        LogRecod.print("Taking out data is ok");
        kivaList.forEach(kiva -> {
            if (kiva != null) {
                KivaSimple simple = new KivaSimple(kiva);
                String des = kiva.getTranslatedDescription();
                if (StringUtils.isNull(des)) {
                    des = kiva.getOriginal_description();
                }
                des = removeHtmlTag(des);
                List<String> ct = NLPIRUtil.cutwords(des);
                ct = NLPIRUtil.removeStopwords(ct);
                simple.setOriginalDescription(des);
                simple.setStandardDescription(Joiner.on("#").join(ct));
                simple.setTags(normalizeTags(kiva.getTags()));
                try {
                    kivaService.insertKivaSimple(simple);
                } catch (Exception e) {
                    kivaService.updateSimpleSelective(simple);
                }
            }
        });
        return Response.success("");
    }

    private String normalizeTags(String tags) {
        StringBuilder result = new StringBuilder();
        String[] ts = tags.split(",");
        Set<String> items = new HashSet<>();
        for (String t : ts) {
            if (t.contains("#")) {
                if (t.startsWith(" ")) {
                    t = t.substring(1);
                }
                items.add(t);

            }
        }
        for (String item : items) {
            result.append(item.toLowerCase());
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
        List<KivaSimple> kivaSimples = getAllKivaSimpleResult(0);
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
    public Response trainSVMFileAll() throws IOException {
        List<KivaResult> kivaResults = getNormalingTFIDFResults(null, TRAIN_DOC_NUM);
        Map<String, Integer> map = getAllWordMap();
        List<String> allTagWords = getAllTags(TAG_FREQ);

        allTagWords.forEach(tag -> {
            try {
                new File(String.format(trainFilePath, tag)).delete();
            } catch (Exception e) {
                e.printStackTrace();
            }
            kivaResults.forEach(kivaResult -> {
                int classifyType = isTag(kivaResult.getKiva().getId(), tag);
                List<Token> tokenList = getTokenListFromFreqTokens(kivaResult.getTokenList());
                SVMUtil.outPutSVMStandardData(map, String.format(trainFilePath, tag), tokenList, false, classifyType);
            });
        });

        return Response.success("");
    }

    private int isTag(Long kivaId, String tag) {
        KivaSimple kivaSimple = kivaService.selectSimpleById(kivaId);
        if (kivaSimple != null && kivaSimple.getTags().contains(tag)) {
            return 1;
        }
        return 0;
    }

    private List<KivaResult> getNormalingTFIDFResults(String text, int docNum) {
        List<KivaResult> kivaResults = getTFIDFResults(text, docNum);

        for (int i = 0; i < kivaResults.size(); i++) {
            List<FreqToken> tokenLit = kivaResults.get(i).getTokenList();
            double max = -1;
            double min = Double.MAX_VALUE;
            for (int j = 0; j < tokenLit.size(); j++) {
                double weight = tokenLit.get(j).getWeight();
                if (weight > max) {
                    max = weight;
                }
                if (weight < min) {
                    min = weight;
                }
            }
            tokenLit = kivaResults.get(i).getTokenList();
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

    public Response trainSVMFileSingle(String tag,
                                       List<KivaResult> kivaResults,
                                       Map<String, Integer> map) throws IOException {
        if (map == null) {
            map = getAllWordMap();
        }
        if (kivaResults == null) {
            kivaResults = getNormalingTFIDFResults(null, TRAIN_DOC_NUM);
        }
        File tagFile = new File(String.format(trainFilePath, tag));
        tagFile.delete();
        for (KivaResult kivaResult : kivaResults) {
            int classifyType = isTag(kivaResult.getKiva().getId(), tag);
            List<Token> tokenList = getTokenListFromFreqTokens(kivaResult.getTokenList());
            SVMUtil.outPutSVMStandardData(map, trainFilePath, tokenList, false, classifyType);
        }
        return Response.success(map);
    }


    @RequestMapping("svm/train/args")
    public Response svmTrainArgs(@RequestParam("tag") String tag) throws IOException {
        SVMUtil.trainModel(tag);
        return Response.success("true");
    }

    @RequestMapping("svm/train/args/all")
    public Response svmTrainArgsAll() throws IOException {
        List<String> tags = getAllTags(TAG_FREQ);
        tags.forEach(tag -> {
            if (!StringUtils.isNull(tag)) {
                try {
                    SVMUtil.trainModel(tag);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        return Response.success("true");
    }


    private void generatorTestData(String text, List<KivaResult> kivaResults) {

        if (kivaResults == null) {
            kivaResults = getNormalingTFIDFResults(text, TRAIN_DOC_NUM);
        }
        KivaResult kivaResult = new KivaResult();
        for (KivaResult result : kivaResults) {
            if (result.getKiva().getId() == -1L) {
                kivaResult = result;
            }
        }
        Map<String, Integer> map = getAllWordMap();
        List<Token> tokens = getTokenListFromFreqTokens(kivaResult.getTokenList());
        SVMUtil.outPutSVMStandardData(map, testFilePath, tokens, true, -1);
    }

    @RequestMapping("svm/generatorTestData")
    public Response generatorTestData(@RequestParam("id") long id,
                                      @RequestParam(value = "kr", required = false) List<KivaResult> kivaResults,
                                      @RequestParam(value = "map", required = false) Map<String, Integer> map) throws IOException {
        if (kivaResults == null) {
            kivaResults = getNormalingTFIDFResults(null, TRAIN_DOC_NUM);
        }
        if (map == null) {
            map = getAllWordMap();
        }
        KivaResult kr = null;
        for (KivaResult kivaResult : kivaResults) {
            if (kivaResult.getKiva().getId() == id) {
                kr = kivaResult;
                break;
            }
        }
        List<Token> tokens = getTokenListFromFreqTokens(kr.getTokenList());
        SVMUtil.outPutSVMStandardData(map, testFilePath, tokens, true, -1);
        return Response.success("true");
    }

    @RequestMapping("svm/classify/all")
    public Response svmClassifyAll(int type) throws IOException {
        List<KivaSimple> kivaSimples = getAllKivaSimpleResult(0);
        List<KivaResult> kivaResults = getNormalingTFIDFResults(null, TRAIN_DOC_NUM);
        Map<String, Integer> map = getAllWordMap();
        //转化为svm文件
        List<String> tagsList = getAllTags(TAG_FREQ);
        for (String tag : tagsList) {
            trainSVMFileSingle(tag, kivaResults, map);
        }
        //训练参数
        svmTrainArgsAll();
        //分类
        kivaSimples.forEach(kivaSimple -> {
            try {
                //产生测试文件
                generatorTestData(kivaSimple.getId(), kivaResults, map);

                GenTag g = JSONObject.parseObject(kivaSimple.getGenTags(), GenTag.class);
                List<Token> tags = (type == TagType.SVM_KNN) ? g.getKnn() : g.getLda();
                List<Token> svmTags = getSVMResultFromTokenList(tags, kivaResults, map);
                updateDatabaseOfGenTag(kivaSimple.getId(), svmTags, type);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        return Response.success("");
    }


    @RequestMapping("svm/classify")
    public Response svmClassify(@RequestParam("tag") String tag) throws IOException {
        File file = new File(String.format(modelFilePath, tag));
        if (!file.exists()) {
            return Response.error(ResultEnum.FAIL_PARAM_WRONG);
        }
        SVMUtil.predict(tag);
        return Response.success(SVMUtil.genSvmResult(tag));
    }

    private Map<String, Integer> getAllTags() {
        List<KivaSimple> kivaSimples = kivaService.selectAllSimple(0);
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
            List<Token> genTags = sublist(getTagsByTagType(tagType, genTag), F_VALUE_NUM);
            precise += same(genTags, originalTokens);
            all += genTags.size();
        }
        return precise / (double) all;
    }

    @RequestMapping("fValue")
    public Response fvalue(int type) throws IOException {
        List<KivaSimple> kivaSimples = kivaService.selectAllSimple(0);
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
            List<Token> genTags = sublist(getTagsByTagType(tagType, genTag), F_VALUE_NUM);
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

            case TagType.LDA:
                genFreqTags = genTag.getLda();
                break;
            case TagType.SVM_KNN:
                genFreqTags = genTag.getSvmKnn();
                break;
            case TagType.SVM_LDA:
                genFreqTags = genTag.getSvmLda();
                break;
            case TagType.FILTER_KNN:
                genFreqTags = genTag.getFilterKnn();
                break;
            case TagType.FILTER_LDA:
                genFreqTags = genTag.getFilterLda();
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
            case TagType.FILTER_KNN:
                response = genTagByFilterKnn(num);

                break;
            case TagType.FILTER_LDA:
                response = genTagByFilterLda(num);

                break;
            case TagType.SVM_KNN:
                response = genTagBySvmKnn(num);
                break;
        }
        return response;
    }

    private Response genTagBySvmKnn(int num) {
        try {
            return svmClassifyAll(TagType.SVM_KNN);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Response.error(ResultEnum.FAIL_PARAM_WRONG);
    }

    private Response genTagByFilterLda(int num) {
        List<KivaSimple> kivaList = kivaService.selectAllSimple(0);
        List<KivaResult> kivaResults = getTFIDFResults(null, 0);
        List<List<Double>> docThetas = getLdaScores(kivaList);
        for (int i = 0; i < kivaList.size(); i++) {
            KivaSimple kivaSimple = kivaList.get(i);
            List<Double> score = docThetas.get(i);
            KivaResult kivaResult = new KivaResult(kivaSimple);
            List<KivaResult> selectResult = getLDASimilarDoc(kivaResult, kivaList, score, docThetas, num);
            List<Token> sortedTags = getSortedTags(selectResult);
            List<Token> tokenList = getSimlarDocTokens(null, sortedTags, kivaResults);
            updateDatabaseOfGenTag(kivaResult.getKiva().getId(), tokenList, TagType.FILTER_LDA);
        }
        return Response.success(docThetas);
    }

    private Response genTagByFilterKnn(int num) {
        List<KivaResult> kivaResults = getTFIDFResults(null, 0);
        kivaResults.forEach(kivaResult -> {
            Collections.sort(kivaResult.getTokenList(), Comparator.comparing(Token::getWeight));
            List<KivaResult> selectResult = getKnnSimilarDoc(kivaResult, kivaResults, num);
            List<Token> sortedTags = getSortedTags(selectResult);
            List<Token> tokenList = getSimlarDocTokens(null, sortedTags, kivaResults);
            tokenList = sublist(tokenList, num);
            updateDatabaseOfGenTag(kivaResult.getKiva().getId(), tokenList, TagType.FILTER_KNN);
        });
        LogRecod.print("knn finished");
        return Response.success("");
    }

    private List<Token> sublist(List<Token> tokenList, int num) {
        if (tokenList.size() < num) {
            return tokenList;
        }
        return tokenList.subList(0, num);
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
            case TagType.SVM_KNN:
                result = genTagForNewTextBySVMKnn(text, endNum);
                break;
            case TagType.SVM_LDA:
                result = genTagForNewTextBySVMLda(text, endNum);
                break;
            case TagType.FILTER_KNN:
                result = genTagForNewTextByFilterKnn(text, endNum);
                break;
            case TagType.FILTER_LDA:
                result = genTagForNewTextByFilterLda(text, endNum);
                break;
        }
        SortedClusterResult clusterResult = getSortedClusterResult(startNum, endNum, result);
        int bp = clusterResult.getBreakPoint();
        Map<String, Object> r = new HashMap<>();
        r.put("knn", genTagForNewTextByKnn(text, endNum));
        r.put("statistics", clusterResult);
        r.put("list", result.subList(0, bp + startNum + 1));
        r.put("original_data", result);
        return Response.success(r);
    }

    private List<Token> genTagForNewTextByFilterKnn(String text, int endNum) {
        List<Token> tokens = genTagForNewTextByKnn(text, endNum);
        List<Token> result = getSimlarDocTokens(text, tokens, null);
        return result;
    }

    private List<Token> genTagForNewTextByFilterLda(String text, int endNum) {
        List<Token> tokens = genTagForNewTextByLda(text, endNum);
        List<Token> result = getSimlarDocTokens(text, tokens, null);
        return result;
    }

    private List<Token> genTagForNewTextBySVMLda(String text, int endNum) {
        List<Token> knnResult = genTagForNewTextByLda(text, endNum);
        List<Token> svmTags = getTokensFromSvmResult(text, knnResult);
        return svmTags;
    }


    private List<Token> genTagForNewTextBySVMKnn(String text, int endNum) {
        List<Token> knnResult = genTagForNewTextByKnn(text, endNum);
        List<Token> svmTags = getTokensFromSvmResult(text, knnResult);
        return svmTags;
    }

    private List<Token> getTokensFromSvmResult(String text, List<Token> knnResult) {
        List<KivaResult> kivaResults = getNormalingTFIDFResults(text, TRAIN_DOC_NUM);
        generatorTestData(text, kivaResults);
        Map<String, Integer> map = getAllWordMap();
        return getSVMResultFromTokenList(knnResult, kivaResults, map);
    }

    private List<Token> getSVMResultFromTokenList(List<Token> knnResult, List<KivaResult> kivaResults,
                                                  Map<String, Integer> map) {
        List<Token> svmTags = new ArrayList<>();
        knnResult.forEach(t -> {
            try {
                svmClassify(t.getWord());
                double score = SVMUtil.genSvmResult(t.getWord());
                if (score > 0.4) {
                    svmTags.add(t);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        return svmTags;
    }


    private List<Token> genTagForNewTextByLda(String text, int endNum) {
        List<KivaSimple> kivaSimples = getAllKivaSimpleResult(0);
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
        List<KivaResult> kivaResults = getTFIDFResults(text, 0);
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
        List<KivaResult> kivaResults = getTFIDFResults(text, 0);
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
            case TagType.SVM_KNN:
                list = genTag.getSvmKnn();
                break;
            case TagType.SVM_LDA:
                list = genTag.getSvmLda();
                break;
            case TagType.FILTER_KNN:
                list = genTag.getFilterKnn();
                break;
            case TagType.FILTER_LDA:
                list = genTag.getFilterLda();
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
        if (startNum < 1) {
            startNum = 1;
        }
        if (list.size() == 0) {
            return new SortedClusterResult();
        }
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
        List<KivaSimple> kivaList = kivaService.selectAllSimple(0);

        List<List<Double>> docThetas = getLdaScores(kivaList);
        for (int i = 0; i < kivaList.size(); i++) {
            KivaSimple kivaSimple = kivaList.get(i);
            List<Double> score = docThetas.get(i);
            KivaResult kivaResult = new KivaResult(kivaSimple);
            List<KivaResult> selectResult = getLDASimilarDoc(kivaResult, kivaList, score, docThetas, num);
            List<Token> sortedTags = getSortedTags(selectResult);
            sortedTags = sublist(sortedTags, num);
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
        List<KivaSimple> simples = getAllKivaSimpleResult(0);
        Map<Integer, Integer> map = new HashMap<>();
        simples.forEach(simple -> {
            List<String> tags = NLPIRUtil.split(simple.getTags(), TagType.TAG_SPLITTER);

            if (map.containsKey(tags.size())) {
                map.put(tags.size(), map.get(tags.size()) + 1);
            } else {
                map.put(tags.size(), 1);
            }
        });
        return Response.success(map);
    }

    @RequestMapping("sim")
    public Response sim() {
        String text = "Hilda Flor is 29 years old. She lives in a rented house with her son under her care in the district of Pucará, located in Jaén province, which belongs to Cajamarca Region in the North Andes of Peru. The people in this community work in the fruit business and raise livestock.Hilda works selling seasonal fruit. Her business is located in the back part of her house. She has requested a loan of 3000 PEN to buy a greater quantity of fruit to offer to her customers.";

        List<Token> toCompare = genTagForNewTextByKnn(text, 10);

        List<Token> result = getSimlarDocTokens(text, toCompare, null);
        Map<String, Object> m = new HashMap<>();
        m.put("knn", toCompare);
        m.put("result", result);
        return Response.success(m);
    }

    private List<Token> getSimlarDocTokens(String text, List<Token> toCompare, List<KivaResult> kivaResults) {
        if (kivaResults == null) {
            kivaResults = getTFIDFResults(text, 0);
        }

        List<Token> result = new ArrayList<>();
        for (Token token : toCompare) {
            List<Long> validIds = simplesContainsTag(token.getWord());
            List<KivaResult> krs = filterValid(kivaResults, validIds);
            double score = computeToSimlerResult(krs, toCompare);
            if (score > 0.005) {
//                Token t = new Token(token.getWord());
//                t.setWeight(score);
                result.add(token);
            }
        }
        Collections.sort(result, Comparator.comparing(Token::getWeight).reversed());

        return result;
    }

    private double computeToSimlerResult(List<KivaResult> kivaResults, List<Token> toCompare) {
        if (kivaResults.size() == 0) {
            return -1;
        }
        double score = 0.0;
        for (KivaResult kivaResult : kivaResults) {
            List<Token> tokenList = Lists.transform(kivaResult.getTokenList(), Token::new);
            score += DistanceUtils.cosDistanceMin(tokenList, toCompare);
        }
        return score / kivaResults.size();
    }

    private List<KivaResult> filterValid(List<KivaResult> kivaResults, List<Long> validIds) {
        return kivaResults.stream().filter(kivaResult -> validIds.contains(kivaResult.getKiva().getId())).collect(Collectors.toList());
    }

    public List<Long> simplesContainsTag(String tag) {
        return kivaService.selectSimpleIdLikeTag(tag);
    }
}
