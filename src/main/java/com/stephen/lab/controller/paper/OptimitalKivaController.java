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
import com.stephen.lab.util.Progress.Progress;
import com.stephen.lab.util.nlp.ClusterUtils;
import com.stephen.lab.util.nlp.KeywordExtractUtils;
import com.stephen.lab.util.nlp.NLPIRUtil;
import com.stephen.lab.util.nlp.classify.SVMUtil;
import com.stephen.lab.util.nlp.lda.sample.conf.ConstantConfig;
import com.stephen.lab.util.nlp.lda.sample.main.Documents;
import com.stephen.lab.util.nlp.lda.sample.main.LdaGibbsSampling;
import com.stephen.lab.util.nlp.lda.sample.main.LdaModel;
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

import static com.stephen.lab.controller.others.ZhouZhaoTaoController.thetaFile;
import static com.stephen.lab.controller.others.ZhouZhaoTaoController.twordsFile;
import static com.stephen.lab.util.nlp.lda.sample.main.LdaGibbsSampling.getParametersFromFile;
import static com.stephen.lab.util.nlp.lda.sample.main.LdaGibbsSampling.setTopicNum;

/**
 * @author Stephen
 */
@RestController
@RequestMapping("kiva-opt")
public class OptimitalKivaController {
    @Autowired
    private KivaService kivaService;
    @Autowired
    private CrawlErrorService crawlErrorService;
    private static final int PAGE_TOTAL = 71270;
    private static final int BUFFER_SIZE = 1024;
    private static final int TRAIN_DOC_NUM = 1000;
    private String allwordPath = "C:\\Users\\Stephen\\Desktop\\svm\\allwords.txt";
    private String trainFilePath = "C:\\Users\\Stephen\\Desktop\\svm\\train\\train-%s.txt";
    private String modelFilePath = "C:\\Users\\Stephen\\Desktop\\svm\\model\\model-%s.txt";
    private String testFilePath = "C:\\Users\\Stephen\\Desktop\\svm\\test\\testdata.txt";
    private static final int TAG_FREQ = 1;
    private String TAF_FREQ = "C:\\Users\\Stephen\\Desktop\\paper\\tag-num-data.txt";

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
                HttpResponse response;
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

    private Response genTagByTextRank(List<KivaSimple> kivaSimples, int num) throws IOException {
        kivaSimples.forEach(kivaSimple -> {
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
    public Response genTagByTfIdf(List<KivaResult> kivaResults, int num) {
        kivaResults.forEach(result -> {
            result.getTokenList().sort(Comparator.comparing(Token::getWeight).reversed());
            List<FreqToken> freqTokens = (result.getTokenList().size() < num ? result.getTokenList() :
                    result.getTokenList().subList(0, num));
            List<Token> resultTokenList = getTokenListFromFreqTokens(freqTokens);
            updateDatabaseOfGenTag(result.getKiva().getId(), resultTokenList, TagType.TFIDF);
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


    private Response genTagByKnn(List<KivaResult> tfIdfResults, @RequestParam("num") int num) throws IOException {
        tfIdfResults.forEach(kivaResult -> {
            KivaSimple simple = new KivaSimple(kivaResult);
            List<Token> sortedTags = genTagForNewTextByKnn(simple, tfIdfResults, num);
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
            case TagType.EXPANDRANK:
                genTag.setExpandRank(newTags);
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
            case TagType.MIXTURE:
                genTag.setMixture(newTags);
                break;
            default:
        }
        simple.setGenTags(JSONObject.toJSONString(genTag));
        kivaService.updateSimpleSelective(simple);
    }

    private List<KivaResult> getTFIDFResults(List<KivaSimple> simpleList, boolean needProgress) {
        LogRecod.print("tfidf 开始计算");
        List<KivaResult> kivaResults = new ArrayList<>();
        Map<String, Integer> yearWordMap = new HashMap<>();
        for (KivaSimple kivaSimple : simpleList) {
            String description = kivaSimple.getStandardDescription();
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
            KivaResult kivaResult = new KivaResult(kivaSimple);
            kivaResult.setTokenList(tokens);
            kivaResults.add(kivaResult);
            double percent = kivaResults.size() / (double) simpleList.size() * 0.15;
            if (needProgress) {
                Progress.put(-1, 15 + Double.parseDouble(DoubleUtils.twoBit(percent)) * 100);
            }
        }
        LogRecod.print("tfidf 词汇频次计算完毕");
        if (needProgress) {
            Progress.put(-1, 25);
        }
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
        int size = toCompare.size();
        double percent;
        for (int i = 0; i < size; i++) {
            KivaResult to = toCompare.get(i);
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
            percent = (i / (double) size) * 0.5;
            Progress.put(-1, 30 + Double.parseDouble(DoubleUtils.twoBit(percent)) * 100);
        }
        result.sort(Comparator.comparing(KivaResult::getDistanceToOther));
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
        result.sort(Comparator.comparing(KivaResult::getDistanceToOther));
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
        List<KivaResult> kivaResults = getTFIDFResults(getAllKivaSimpleResult(TRAIN_DOC_NUM), false);
        List<KivaResult> normalingTFIDFResults = getNormalingTFIDFResults(kivaResults);
        Map<String, Integer> map = getAllWordMap();
        List<String> allTagWords = getAllTags(TAG_FREQ);
        allTagWords.forEach(tag -> {
            try {
                boolean result = new File(String.format(trainFilePath, tag)).delete();
                if (!result) {
                    LogRecod.print("error,file could not delete");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            for (KivaResult kivaResult : normalingTFIDFResults) {
                int classifyType = isTag(kivaResult.getKiva().getId(), tag);
                List<Token> tokenList = getTokenListFromFreqTokens(kivaResult.getTokenList());
                SVMUtil.outPutSVMStandardData(map, String.format(trainFilePath, tag), tokenList, false, classifyType);
            }
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

    private List<KivaResult> getNormalingTFIDFResults(List<KivaResult> kivaResults) {
        for (KivaResult kivaResult : kivaResults) {
            List<FreqToken> tokenLit = kivaResult.getTokenList();
            double max = -1;
            double min = Double.MAX_VALUE;
            for (FreqToken aTokenLit : tokenLit) {
                double weight = aTokenLit.getWeight();
                if (weight > max) {
                    max = weight;
                }
                if (weight < min) {
                    min = weight;
                }
            }
            tokenLit = kivaResult.getTokenList();
            for (FreqToken token : tokenLit) {
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


    private void generatorTestData(KivaSimple simple, List<KivaResult> kivaResults) {
        KivaResult kivaResult = null;
        for (KivaResult result : kivaResults) {
            if (result.getKiva().getId().equals(simple.getId())) {
                kivaResult = result;
                break;
            }
        }
        if (kivaResult == null) {
            return;
        }
        Map<String, Integer> map = getAllWordMap();
        List<Token> tokens = getTokenListFromFreqTokens(kivaResult.getTokenList());
        SVMUtil.outPutSVMStandardData(map, testFilePath, tokens, true, -1);
    }

    @RequestMapping("svm/generatorTestData")
    public void generatorTestData(@RequestParam("id") long id,
                                  @RequestParam(value = "kr", required = false) List<KivaResult> kivaResults,
                                  @RequestParam(value = "map", required = false) Map<String, Integer> map) throws IOException {
        if (kivaResults == null) {
            kivaResults = getTFIDFResults(getAllKivaSimpleResult(TRAIN_DOC_NUM), false);
            kivaResults = getNormalingTFIDFResults(kivaResults);
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
        List<Token> tokens = getTokenListFromFreqTokens(kr != null ? kr.getTokenList() : null);
        SVMUtil.outPutSVMStandardData(map, testFilePath, tokens, true, -1);
        Response.success("true");
    }

    @RequestMapping("svm/classify/all")
    public Response svmClassifyAll(int type) throws IOException {
        List<KivaSimple> kivaSimples = getAllKivaSimpleResult(TRAIN_DOC_NUM + 250);
        List<KivaResult> tmp = getTFIDFResults(kivaSimples, false);
        List<KivaResult> kivaResults = getNormalingTFIDFResults(tmp);
        Map<String, Integer> map = getAllWordMap();
        //转化为svm文件
//        List<String> tagsList = getAllTags(TAG_FREQ);
//        for (String tag : tagsList) {
//            trainSVMFileSingle(tag, kivaResults, map);
//        }
        //训练参数
//        svmTrainArgsAll();
        //分类
        for (int i = TRAIN_DOC_NUM; i < TRAIN_DOC_NUM + 250; i++) {
            KivaSimple kivaSimple = kivaSimples.get(i);
            try {
                //产生测试文件
                generatorTestData(kivaSimple.getId(), kivaResults, map);

                GenTag g = JSONObject.parseObject(kivaSimple.getGenTags(), GenTag.class);
                List<Token> tags = (type == TagType.SVM_KNN) ? g.getKnn() : g.getLda();
                List<Token> svmTags = getSVMResultFromTokenList(tags);
                updateDatabaseOfGenTag(kivaSimple.getId(), svmTags, type);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return Response.success("");
    }


    @RequestMapping("svm/classify")
    public void svmClassify(@RequestParam("tag") String tag) throws IOException {
        File file = new File(String.format(modelFilePath, tag));
        if (!file.exists()) {
            Response.error(ResultEnum.FAIL_PARAM_WRONG);
            return;
        }
        SVMUtil.predict(tag);
        Response.success(SVMUtil.genSvmResult(tag));
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

    private double pValue(List<KivaSimple> simpleList, int tagType, int num) throws IOException {
        int all = 0;
        int precise = 0;
        for (KivaSimple kivaSimple : simpleList) {
            GenTag genTag = JSONObject.parseObject(kivaSimple.getGenTags(), GenTag.class);
            List<String> originTags = splitString(kivaSimple.getTags());
            List<Token> originalTokens = Lists.transform(originTags, Token::new);
            List<Token> genTags = sublist(getTagsByTagType(tagType, genTag), num);
            precise += same(genTags, originalTokens);
            all += genTags.size();
        }
        return precise / (double) all;
    }

    @RequestMapping("fValue")
    public Response fvalue(int type, int num) throws IOException {
        List<KivaSimple> kivaSimples = kivaService.selectAllSimple(0);
//        if(type==5){
        kivaSimples = kivaSimples.stream().filter(kivaSimple -> kivaSimple.getGenTags().contains("mixture")).collect(Collectors.toList());
        LogRecod.print(kivaSimples.size());
//        }
        double p = pValue(kivaSimples, type, num);
        double r = rValue(kivaSimples, type, num);
        double f = 2 * (p * r) / (p + r);
        Map<String, Object> result = new HashMap<>();
        result.put("p", p);
        result.put("r", r);
        result.put("f", f);
        return Response.success(result);

    }

    private double rValue(List<KivaSimple> simpleList, int tagType, int num) throws IOException {
        int all = 0;
        int recall = 0;
        for (KivaSimple kivaSimple : simpleList) {
            GenTag genTag = JSONObject.parseObject(kivaSimple.getGenTags(), GenTag.class);
            List<String> originTags = splitString(kivaSimple.getTags());
            List<Token> originalTokens = Lists.transform(originTags, Token::new);
            List<Token> genTags = sublist(getTagsByTagType(tagType, genTag), num);
            recall += same(genTags, originalTokens);
            all += originTags.size();
        }
        return recall / (double) all;
    }

    private List<Token> getTagsByTagType(int tagType, GenTag genTag) {
        List<? extends Token> genFreqTags;
        switch (tagType) {
            case TagType.TFIDF:
                genFreqTags = genTag.getTfIdf();
                break;
            case TagType.TEXTRANK:
                genFreqTags = genTag.getTextRank();
                break;
            case TagType.EXPANDRANK:
                genFreqTags = genTag.getExpandRank();
                break;
            case TagType.KNN:
                genFreqTags = genTag.getKnn();
                break;
            case TagType.LDA:
                genFreqTags = genTag.getLda();
                break;
            case TagType.SVM_KNN:
                List<Token> knTokens = genTag.getKnn();
                List<Token> svmKnn = genTag.getSvmKnn();
                knTokens.forEach(t -> {
                    int index = svmKnn.indexOf(t);
                    if (index != -1) {
                        t.setWeight(t.getWeight() * svmKnn.get(index).getWeight());
                    }
                });
                knTokens.sort(Comparator.comparing(Token::getWeight).reversed());
                genFreqTags = knTokens;
                break;
            case TagType.SVM_LDA:
                List<Token> lTokens = genTag.getLda();
                List<Token> svmLda = genTag.getSvmLda();
                lTokens.forEach(t -> {
                    int index = svmLda.indexOf(t);
                    if (index != -1) {
                        t.setWeight(t.getWeight() * svmLda.get(index).getWeight());
                    }
                });
                lTokens.sort(Comparator.comparing(Token::getWeight).reversed());
                genFreqTags = lTokens;
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
        for (Token l : ls) {
            if (lb.contains(l)) {
                count++;
            }
        }
        return count;
    }

    @RequestMapping("mixture-tag-num")
    public Response mixNum() throws FileNotFoundException {
        List<KivaSimple> kivaSimples = kivaService.selectAllSimple(0);
        kivaSimples = kivaSimples.stream().filter(kivaSimple -> kivaSimple.getGenTags().contains("mixture")).collect(Collectors.toList());
        Map<String, Integer> map = new HashMap<>();
        new File(TAF_FREQ).delete();
        BufferedWriter writer = Files.newWriter(new File(TAF_FREQ), Charsets.UTF_8);
        for (KivaSimple kivaSimple : kivaSimples) {
            GenTag genTag = JSONObject.parseObject(kivaSimple.getGenTags(), GenTag.class);
            List<Token> tokens = genTag.getMixture();
            if (tokens != null) {
                tokens.forEach(token -> {
                    if (map.containsKey(token.getWord())) {
                        map.put(token.getWord(), map.get(token.getWord()) + 1);
                    } else {
                        map.put(token.getWord(), 1);
                    }
                });
            }
        }
        Map<String, Integer> tags = getAllTags();
        LogRecod.print(tags.size());
        Map<String, Integer> resultMap = MapUtils.sortMapByValue(map, true);
        for (Map.Entry<String, Integer> m : resultMap.entrySet()) {
            if (m.getKey().charAt(0) >= 'A' && m.getKey().charAt(0) <= 'Z' && m.getValue() <= 6) {
            } else {
                try {
                    writer.append(m.getKey())
                            .append("\t")
                            .append(String.valueOf(m.getValue()))
                            .append("\t")
                            .append("" + tags.containsKey(m.getKey()))
                            .append("\r\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        try {
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Response.success(resultMap);
    }

    @RequestMapping("gen-tag")
    public Response genTag(int type, int num) throws IOException {
        Response response = Response.error(ResultEnum.FAIL_PARAM_WRONG);
        List<KivaSimple> kivaSimpleList = getAllKivaSimpleResult(0);
        List<KivaResult> tfIdfResults = getTFIDFResults(kivaSimpleList, false);
        switch (type) {
            case TagType.TFIDF:
                response = genTagByTfIdf(tfIdfResults, num);
                break;
            case TagType.TEXTRANK:
                response = genTagByTextRank(kivaSimpleList, num);
                break;
            case TagType.EXPANDRANK:
                response = genTagByExpandRank(tfIdfResults, num);
                break;
            case TagType.KNN:
                response = genTagByKnn(tfIdfResults, num);
                break;
            case TagType.LDA:
                response = genTagByLDA(kivaSimpleList, num);
                break;
            case TagType.SVM_KNN:
                response = genTagBySvmKnn();
                break;
            case TagType.SVM_LDA:
                response = genTagBySvmLda();
                break;
            case TagType.MIXTURE:
                response = genTagByMixtrue(kivaSimpleList, tfIdfResults);
            default:
                break;
        }
        return response;
    }

    private Response genTagByMixtrue(List<KivaSimple> kivaSimpleList, List<KivaResult> tfIdfResults) {
        kivaSimpleList.forEach(kivaSimple -> {
            List<Token> result = getTagForNewTextByMixture(kivaSimple.getId(), 2, 8,
                    4, kivaSimple, tfIdfResults);
            updateDatabaseOfGenTag(kivaSimple.getId(), result, TagType.MIXTURE);
        });

        return null;
    }

    private Response genTagByExpandRank(List<KivaResult> tfIdfResults, int num) {
        tfIdfResults.forEach(kivaResult -> {
            KivaSimple kivaSimple = new KivaSimple(kivaResult);
            List<Token> tokens = genTagForNewTextByExpandrank(kivaSimple, tfIdfResults, num);
            updateDatabaseOfGenTag(kivaResult.getKiva().getId(), tokens, TagType.EXPANDRANK);
        });
        return Response.success(true);

    }

    @RequestMapping("get-tag-num")
    public Response genTagNum(int type) throws IOException {
        List<KivaSimple> kivaSimples = getAllKivaSimpleResult(0);
//        kivaSimples = kivaSimples.stream().filter(kivaSimple -> kivaSimple.getGenTags().contains("svm")).collect(Collectors.toList());
        List<List<Token>> listList = new ArrayList<>();
        kivaSimples.forEach(kivaSimple -> {
            try {
                GenTag genTag = JSONObject.parseObject(kivaSimple.getGenTags(), GenTag.class);
                List<Token> tokenList = getTagsByTagType(type, genTag);
                listList.add(tokenList);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        Map<Integer, Integer> result = getSortedClusterNumResult(listList);
        return Response.success(result);
    }

    private Map<Integer, Integer> getSortedClusterNumResult(List<List<Token>> listList) {
        Map<Integer, Integer> result = new HashMap<>();
        listList.forEach(list -> {
            SortedClusterResult r = ClusterUtils.sortedCluster(list);
            if (result.containsKey(r.getBreakPoint())) {
                result.put(r.getBreakPoint(), result.get(r.getBreakPoint()) + 1);
            } else {
                result.put(r.getBreakPoint(), 1);
            }
        });
        return result;
    }

    private Response genTagBySvmLda() {
        try {
            return svmClassifyAll(TagType.SVM_LDA);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Response.success(true);
    }

    private Response genTagBySvmKnn() {
        try {
            return svmClassifyAll(TagType.SVM_KNN);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Response.error(ResultEnum.FAIL_PARAM_WRONG);
    }

    private List<Token> sublist(List<Token> tokenList, int num) {
        if (tokenList.size() < num) {
            return tokenList;
        }
        return tokenList.subList(0, num);
    }

    @RequestMapping("gen-tag-single")
    public Response genTagSingle(@RequestParam(value = "text") String text,
                                 @RequestParam(value = "type", defaultValue = "2") int type,
                                 @RequestParam(value = "startNum", defaultValue = "2") int startNum,
                                 @RequestParam(value = "endNum", defaultValue = "5") int endNum,
                                 @RequestParam(value = "filter", defaultValue = "off") String filter,
                                 @RequestParam(value = "ratio", defaultValue = "2") int ratio) throws IOException {
        if (StringUtils.isNull(text)) {
            Progress.put(-1, 100);
            return Response.error(ResultEnum.FAIL_PARAM_WRONG);
        }
        if ("on".equals(filter)) {
            if (type == TagType.KNN) {
                type = TagType.SVM_KNN;
            }
            if (type == TagType.LDA) {
                type = TagType.SVM_LDA;
            }
        }
        if (StringUtils.isNull(text)) {
            return Response.error(ResultEnum.FAIL_PARAM_WRONG);
        }
        KivaSimple simple = new KivaSimple();
        simple.setId(-1L);
        simple.setOriginalDescription(text);
        try {
            simple.setStandardDescription(Joiner.on("#").join(cutwords(text)));
        } catch (IOException e) {
            e.printStackTrace();
            return Response.error(ResultEnum.FAIL_PARAM_WRONG);
        }
        Progress.put(-1, 5);
        LogRecod.print(simple.getStandardDescription());
        List<KivaSimple> kivaSimpleList = getAllKivaSimpleResult(0);
        kivaSimpleList.add(simple);
        List<KivaResult> kivaResults = getTFIDFResults(kivaSimpleList, true);

        List<Token> result = new ArrayList<>();
        Progress.put(-1, 10);

        switch (type) {
            case TagType.TFIDF:
                result = genTagForNewTextByTfIdf(simple, kivaResults, endNum);
                break;
            case TagType.TEXTRANK:
                result = genTagForNewTextByTextrank(simple, endNum);
                break;
            case TagType.EXPANDRANK:
                result = genTagForNewTextByExpandrank(simple, kivaResults, endNum);
                break;
            case TagType.KNN:
                result = genTagForNewTextByKnn(simple, kivaResults, endNum);
                break;
            case TagType.LDA:
                List<List<Double>> docThetas = getLdaScores(kivaSimpleList);
                List<Double> scores = docThetas.get(docThetas.size() - 1);
                result = genTagForNewTextByLda(docThetas, scores, simple, kivaSimpleList, endNum);
                break;
            case TagType.SVM_KNN:
                result = genTagForNewTextBySVMKnn(null, simple, kivaResults, endNum);
                break;
            case TagType.SVM_LDA:
                result = genTagForNewTextBySVMLda(simple, kivaResults, kivaSimpleList, endNum);
                break;
            default:
                break;
        }
        LogRecod.print(result);
        if (type != TagType.MIXTURE) {
            Progress.put(-1, 80);
            SortedClusterResult clusterResult = getSortedClusterResult(startNum, endNum, result);
            int bp = clusterResult.getBreakPoint();
            Progress.put(-1, 100);
            if (result != null) {
                return Response.success(result.subList(0, bp + startNum + 1));
            }
        } else {
            result = getTagForNewTextByMixture(null, startNum, endNum, ratio, simple, kivaResults);
            Progress.put(-1, 100);

        }
        return Response.success(result);
    }

    @RequestMapping("tag-num-avg")
    public Response tagNumByAvg(int type, double th, boolean useAvg) {
        List<KivaSimple> kivaSimpleList = getAllKivaSimpleResult(0);
        Map<Integer, Integer> numMap = new HashMap<>();
        for (KivaSimple kivaSimple : kivaSimpleList) {
            try {
                GenTag genTag = JSONObject.parseObject(kivaSimple.getGenTags(), GenTag.class);
                List<Token> list = getTokensFromGenTag(type, genTag);
                if (list != null && list.size() > 0) {
                    double sum = 0;
                    for (Token token : list) {
                        sum += token.getWeight();
                    }
                    double avg;
                    if (useAvg) {
                        avg = sum / list.size();
                    } else {
                        avg = th;
                    }
                    list = list.stream().filter(token -> token.getWeight() > avg).collect(Collectors.toList());
                    int num = list.size();
                    if (numMap.containsKey(num)) {
                        numMap.put(num, numMap.get(num) + 1);
                    } else {
                        numMap.put(num, 1);
                    }
                }
            } catch (Exception e) {
                LogRecod.print(kivaSimple.getGenTags());
            }
        }
        return Response.success(numMap);

    }

    private List<Token> getTagForNewTextByMixture(
            Long id,
            @RequestParam(value = "startNum", defaultValue = "2") int startNum, @RequestParam(value = "endNum", defaultValue = "5") int endNum, @RequestParam(value = "ratio", defaultValue = "2") int ratio, KivaSimple simple, List<KivaResult> kivaResults) {
        List<Token> result = new ArrayList<>();
        int tStartNum = startNum / ratio;
        int tEndNum = endNum / ratio;
        List<Token> textRankList;
        List<Token> knnRankList = null;
        if (id != null) {
            KivaSimple s = kivaService.selectSimpleById(id);
            GenTag genTag = JSONObject.parseObject(s.getGenTags(), GenTag.class);
            textRankList = getTagsByTagType(TagType.TEXTRANK, genTag);
            knnRankList = getTagsByTagType(TagType.KNN, genTag);
        } else {
            textRankList = genTagForNewTextByTextrank(simple, tEndNum);
        }
        SortedClusterResult tClusterResult = getSortedClusterResult(tStartNum, tEndNum, textRankList);
        result.addAll(textRankList.subList(0, tClusterResult.getBreakPoint() + tStartNum + 1));
        int sStartNum = startNum - tStartNum;
        int sEndNum = endNum - tEndNum;
        List<Token> svmKnnList = genTagForNewTextBySVMKnn(knnRankList, simple, kivaResults, 10 * sEndNum);
        SortedClusterResult sClusterResult = getSortedClusterResult(sStartNum, sEndNum, svmKnnList);
        if (svmKnnList.size() > 0) {
            if (svmKnnList.size() <= sClusterResult.getBreakPoint() + sStartNum + 1) {
                result.addAll(svmKnnList);
            } else {
                result.addAll(svmKnnList.subList(0, sClusterResult.getBreakPoint() + sStartNum + 1));
            }
        }
        return result;
    }

    private List<Token> genTagForNewTextByExpandrank(KivaSimple simple, List<KivaResult> kivaResults, int endNum) {
        for (KivaResult kivaResult : kivaResults) {
            if (kivaResult.getKiva().getId().equals(simple.getId())) {
                kivaResult.getTokenList().sort(Comparator.comparing(Token::getWeight));
                List<KivaResult> selectResult = getKnnSimilarDoc(kivaResult, kivaResults, 5);
                StringBuilder sb = new StringBuilder();
                sb.append(simple.getOriginalDescription()).append("#");
                selectResult.forEach(sr -> {
                    KivaSimple s = kivaService.selectSimpleById(sr.getKiva().getId());
                    sb.append(s.getStandardDescription()).append("#");
                });
                String description = sb.toString().replaceAll("#", " ");
                return KeywordExtractUtils.textrank(description, endNum);
            }
        }
        return null;
    }


    private List<Token> genTagForNewTextBySVMLda(KivaSimple simple, List<KivaResult> kivaResults, List<KivaSimple> kivaSimples, int endNum) {
        List<List<Double>> docThetas = getLdaScores(kivaSimples);
        List<Double> scores = docThetas.get(docThetas.size() - 1);
        List<Token> ldaResult = genTagForNewTextByLda(docThetas, scores, simple, kivaSimples, endNum);
        return getTokensFromSvmResult(simple, kivaResults, ldaResult);
    }


    private List<Token> genTagForNewTextBySVMKnn(List<Token> knnResult, KivaSimple simple, List<KivaResult> kivaResults, int endNum) {
        if (knnResult == null) {
            knnResult = genTagForNewTextByKnn(simple, kivaResults, endNum);
        }
        List<Token> svmTags = getTokensFromSvmResult(simple, kivaResults, knnResult);
        svmTags.sort(Comparator.comparing(Token::getWeight).reversed());
        return svmTags;
    }

    private List<Token> getTokensFromSvmResult(KivaSimple simple, List<KivaResult> kivaResults, List<Token> knnResult) {
        List<KivaResult> normalingTFIDFResults = getNormalingTFIDFResults(kivaResults);
        generatorTestData(simple, normalingTFIDFResults);
        return getSVMResultFromTokenList(knnResult);
    }

    private List<Token> getSVMResultFromTokenList(List<Token> knnResult) {
        List<Token> svmTags = new ArrayList<>();
        knnResult.forEach(t -> {
            try {
                svmClassify(t.getWord());
                double score = SVMUtil.genSvmResult(t.getWord());
                t.setWeight(score * t.getWeight());
                svmTags.add(t);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        return svmTags;
    }


    private List<Token> genTagForNewTextByLda(List<List<Double>> docThetas, List<Double> scores, KivaSimple simple, List<KivaSimple> kivaSimples, int endNum) {
        KivaResult kivaResult = new KivaResult(simple);
        List<KivaResult> selectResult = getLDASimilarDoc(kivaResult, kivaSimples,
                scores, docThetas, endNum);
        return getSortedTags(selectResult);
    }

    private List<Token> genTagForNewTextByKnn(KivaSimple simple, List<KivaResult> kivaResults, int endNum) {
        for (KivaResult kivaResult : kivaResults) {
            if (kivaResult.getKiva().getId().equals(simple.getId())) {
                kivaResult.getTokenList().sort(Comparator.comparing(Token::getWeight));
                List<KivaResult> selectResult = getKnnSimilarDoc(kivaResult, kivaResults, endNum);
                return getSortedTags(selectResult);
            }
        }
        return new ArrayList<>();
    }

    private List<Token> genTagForNewTextByTextrank(KivaSimple simple, int endNum) {
        try {
            return KeywordExtractUtils.textrank(Joiner.on(" ").join(cutwords(simple.getOriginalDescription())), endNum);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    private List<Token> genTagForNewTextByTfIdf(KivaSimple simple, List<KivaResult> resultList, int num) {
        for (KivaResult kivaResult : resultList) {
            if (kivaResult.getKiva().getId().equals(simple.getId())) {
                kivaResult.getTokenList().sort(Comparator.comparing(Token::getWeight).reversed());
                List<FreqToken> freqTokens = (kivaResult.getTokenList().size() < num ? kivaResult.getTokenList() :
                        kivaResult.getTokenList().subList(0, num));
                return getTokenListFromFreqTokens(freqTokens);
            }
        }
        return new ArrayList<>();
    }

    @RequestMapping("gen-breakpoint")
    public Response genBreakPoint(long id, int type, int startNum, int endNum) throws IOException {
        KivaSimple kivaSimple = kivaService.selectSimpleById(id);
        GenTag genTag = JSONObject.parseObject(kivaSimple.getGenTags(), GenTag.class);
        List<Token> list = getTokensFromGenTag(type, genTag);
        SortedClusterResult result = getSortedClusterResult(startNum, endNum, list);
        int bp = result.getBreakPoint();
        Map<String, Object> r = new HashMap<>();
        r.put("statistics", result);
        r.put("list", list != null ? list.subList(0, bp + startNum) : null);
        return Response.success(r);
    }

    private List<Token> getTokensFromGenTag(int type, GenTag genTag) {
        List<Token> list = null;
        switch (type) {
            case TagType.TFIDF:
                list = genTag.getTfIdf();
                break;
            case TagType.TEXTRANK:
                list = genTag.getTextRank();
                break;
            case TagType.EXPANDRANK:
                list = genTag.getExpandRank();
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
            case TagType.LDA:
                list = genTag.getLda();
                break;
            default:
                break;
        }
        return list;
    }

    private SortedClusterResult getSortedClusterResult(int startNum, int endNum, List<Token> list) {
        if (startNum < 1) {
            startNum = 1;
        }
        if (list.size() == 0) {
            return new SortedClusterResult();
        }
        List<Token> sublist;
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
    public Response genTagByLDA(List<KivaSimple> kivaSimpleList, int num) {
        List<List<Double>> docThetas = getLdaScores(kivaSimpleList);

        for (int i = 0; i < kivaSimpleList.size(); i++) {
            KivaSimple kivaSimple = kivaSimpleList.get(i);
            List<Token> sortedTags = genTagForNewTextByLda(docThetas, docThetas.get(i),
                    kivaSimple, kivaSimpleList, num);
            updateDatabaseOfGenTag(kivaSimple.getId(), sortedTags, TagType.LDA);
        }
        return Response.success(docThetas);
    }

    @RequestMapping("lda-perplexcity")
    public Response perplexity(int totalNum) {
        if (totalNum < 1) {
            return Response.error(ResultEnum.FAIL_PARAM_WRONG);
        }
        Map<Integer, Double> result = new HashMap<>(totalNum);
        List<KivaSimple> kivaSimpleList = getAllKivaSimpleResult(0);
//        for (int i = 1; i < totalNum; i++) {
        trainAndSaveLdaModel(totalNum, kivaSimpleList);
        double p = LdaModel.getPerplexity(totalNum, thetaFile, twordsFile);
        LogRecod.print(totalNum + "\t" + p);
        result.put(totalNum, p);
//        }
        return Response.success(result);
    }

    private void trainAndSaveLdaModel(Integer topicNum, List<KivaSimple> kivaList) {
        String parameterFile = ConstantConfig.LDAPARAMETERFILE;

        LdaGibbsSampling.modelparameters ldaparameters = new LdaGibbsSampling.modelparameters();
        getParametersFromFile(ldaparameters, parameterFile);
        if (topicNum != null) {
            setTopicNum(ldaparameters, topicNum);
        }
        List<String> contents = Lists.transform(kivaList, KivaSimple::getStandardDescription);
        Documents docSet = new Documents();
        docSet.readDocs(contents);
        System.out.println("wordMap size " + docSet.termToIndexMap.size());
        Progress.put(-1, 20);
        LdaModel model = new LdaModel(ldaparameters);
        System.out.println("1 Initialize the model ...");
        model.initializeModel(docSet);
        Progress.put(-1, 40);
        System.out.println("2 Learning and Saving the model ...");
        try {
            model.inferenceModel(docSet);
            model.saveIteratedModel(ldaparameters.iteration, docSet);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Progress.put(-1, 60);
        System.out.println("3 Output the final model ...");
        System.out.println("Done!");
    }

    private List<List<Double>> getLdaScores(List<KivaSimple> kivaList) {
        trainAndSaveLdaModel(null, kivaList);
        String parameterFile = ConstantConfig.LDAPARAMETERFILE;
        LdaGibbsSampling.modelparameters ldaparameters = new LdaGibbsSampling.modelparameters();
        getParametersFromFile(ldaparameters, parameterFile);
        List<String> contents = Lists.transform(kivaList, KivaSimple::getStandardDescription);
        Documents docSet = new Documents();
        docSet.readDocs(contents);
        System.out.println("wordMap size " + docSet.termToIndexMap.size());
        Progress.put(-1, 20);
        LdaModel model = new LdaModel(ldaparameters);
        System.out.println("1 Initialize the model ...");
        model.initializeModel(docSet);
        Progress.put(-1, 40);
        System.out.println("2 Learning and Saving the model ...");
        try {
            model.inferenceModel(docSet);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Progress.put(-1, 60);
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
        Progress.put(-1, 75);
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

    @RequestMapping("progress")
    public Response progress() {
        Object obj = Progress.get(-1);
        try {
            if (obj != null && Double.parseDouble(obj.toString()) >= 100) {
                Progress.remove(-1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Response.success(obj);
    }

    @RequestMapping("filter-word")
    public Response filterword() throws IOException {
        List<String> words = Files.readLines(new File(allwordPath), Charsets.UTF_8);
        List<String> filter = Lists.newArrayList("VA", "VC", "VE", "VV", "NR", "NT", "NN", "LC", "PN", "AD", "P", "CC", "CS");
        words.forEach(word -> {
            if (NLPIRUtil.cutwords(word.split("\t")[0], filter)) {
                try {
                    Files.append(word + "\r\n", new File(allwordPath + "t"), Charsets.UTF_8);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        return Response.success(Progress.get(-1));
    }
}
