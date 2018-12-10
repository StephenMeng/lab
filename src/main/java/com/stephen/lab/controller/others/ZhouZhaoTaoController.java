package com.stephen.lab.controller.others;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.stephen.lab.constant.semantic.ResultEnum;
import com.stephen.lab.util.LogRecod;
import com.stephen.lab.util.MapUtils;
import com.stephen.lab.util.Response;
import com.stephen.lab.util.StringUtils;
import com.stephen.lab.util.nlp.NLPIRUtil;
import com.stephen.lab.util.nlp.lda.sample.com.FileUtil;
import com.stephen.lab.util.nlp.lda.sample.conf.ConstantConfig;
import com.stephen.lab.util.nlp.lda.sample.conf.PathConfig;
import com.stephen.lab.util.nlp.lda.sample.main.Documents;
import com.stephen.lab.util.nlp.lda.sample.main.LdaGibbsSampling;
import com.stephen.lab.util.nlp.lda.sample.main.LdaModel;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.*;

import static com.stephen.lab.util.nlp.lda.sample.main.LdaGibbsSampling.getParametersFromFile;
import static com.stephen.lab.util.nlp.lda.sample.main.LdaGibbsSampling.setTopicNum;

/**
 * Created by stephen on 2018/3/15.
 */
@RestController
@RequestMapping("zzt")
public class ZhouZhaoTaoController {
    private String testFilePath = "C:\\Users\\Stephen\\Desktop\\lda\\test.txt";
    private String kwFilePath = "C:\\Users\\Stephen\\Desktop\\lda\\kw.txt";
    private String yearData = "C:\\Users\\Stephen\\Desktop\\lda\\year.txt";
    public static String twordsFile = "C:\\Users\\stephen\\Desktop\\lda\\LdaResults\\lda_100.phi";
    public static String thetaFile = "C:\\Users\\stephen\\Desktop\\lda\\LdaResults\\lda_100.theta";

    @RequestMapping("high-freq")
    public Response highFreq() throws IOException {
        String fileInputPath = "C:\\Users\\stephen\\Desktop\\high-freq.txt";
        String fileOutputPath = "C:\\Users\\stephen\\Desktop\\high-freq-result.txt";

        List<String> stringList = Files.readLines(new File(fileInputPath), Charsets.UTF_8);
        Map<String, Integer> wordFreqMap = new HashMap<>();
        stringList.forEach(s -> {
            List<String> strings = Splitter.on(";").trimResults().splitToList(s);
            strings.forEach(string -> {
                if (!StringUtils.isNull(string)) {
                    if (wordFreqMap.containsKey(string.toLowerCase())) {
                        wordFreqMap.put(string.toLowerCase(), wordFreqMap.get(string.toLowerCase()) + 1);
                    } else {
                        wordFreqMap.put(string.toLowerCase(), 1);
                    }
                }
            });
        });
        File outF = new File(fileOutputPath);
        Map<String, Integer> result = MapUtils.sortMapByValue(wordFreqMap, true);
        for (Map.Entry<String, Integer> map : result.entrySet()) {
            Files.append(map.getKey() + "\t" + map.getValue() + "\r\n", outF, Charsets.UTF_8);
        }
        return Response.success("");
    }

    @RequestMapping("lda")
    public Response ldaTest(int topicNum) throws IOException {
        trainLdaModel(topicNum);
//        double perplexity = LdaModel.getPerplexity(0, topicNum, thetaFile, twordsFile);
//        return Response.success(perplexity);
        return Response.success(true);
    }

    private void trainLdaModel(int topicNum) throws IOException {
        String testData = testFilePath;
        String resultPath = PathConfig.LdaResultsPath;
        String parameterFile = ConstantConfig.LDAPARAMETERFILE;

        LdaGibbsSampling.modelparameters ldaparameters = new LdaGibbsSampling.modelparameters();
        getParametersFromFile(ldaparameters, parameterFile);
        setTopicNum(ldaparameters, topicNum);
        List<String> contents = readData(testData);
        Documents docSet = new Documents();
        docSet.readDocs(contents, "");
        System.out.println("wordMap size " + docSet.termToIndexMap.size());
        FileUtil.mkdir(new File(resultPath));
        LdaModel model = new LdaModel(ldaparameters);
        System.out.println("1 Initialize the model ...");
        model.initializeModel(docSet);
        System.out.println("2 Learning and Saving the model ...");
        model.inferenceModel(docSet);
        System.out.println("3 Output the final model ...");
        model.saveIteratedModel(ldaparameters.iteration, docSet);
        System.out.println("Done!");
    }

    @RequestMapping("lda-p")
    public Response ldaPro(int totalNum) throws IOException {
        if (totalNum < 1) {
            return Response.error(ResultEnum.FAIL_PARAM_WRONG);
        }
        Map<Integer, Double> result = new HashMap<>(totalNum);
        for (int i = 1; i < totalNum; i++) {
            trainLdaModel(i);
            double p = LdaModel.getPerplexity(0, i, thetaFile, twordsFile);
            result.put(i, p);
        }
        return Response.success(result);
    }

    private List<String> readData(String testData) throws IOException {
        List<String> stringList = Files.readLines(new File(testData), Charsets.UTF_8);
        List<String> contents = new ArrayList<>();
        List<Integer> yearList = new ArrayList<>();
        for (String keywords : stringList) {
            try {
                String[] strings = keywords.split("\t");
                String kw = strings[2];//0表示关键词，2表示摘要
                kw = kw.replaceAll("\\(", " ");
                kw = kw.replaceAll("\\)", " ");
                kw = kw.toLowerCase();
                LogRecod.print(kw);
                List<String> t = NLPIRUtil.cutwords(kw);
                LogRecod.print(t);
//                t = t.stream().filter(tm -> {
//                    try {
//                        return !NLPIRUtil.getStopwords().contains(tm);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                    return true;
//                }).collect(Collectors.toList());
                kw = Joiner.on(" ").join(t);
                Integer year = Integer.parseInt(strings[1]);
                int index = yearList.indexOf(year);
                if (index != -1) {
                    String tmp = contents.get(index) + kw;
                    contents.set(index, tmp);
                } else {
                    yearList.add(year);
                    contents.add(kw);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        BufferedWriter writer = Files.newWriter(new File(yearData), Charsets.UTF_8);
        for (int i = 0; i < yearList.size(); i++) {
            writer.write(i + "\t" + yearList.get(i) + "\r\n");
        }
        writer.close();
        return contents;
    }

    @RequestMapping("jieba")
    public Response jiebafenci() {
        String content = "在做结巴分词的时候，其中他有提供一个函数是获取一段文本的关键词，然后我想知道要让这些关键词过滤掉一些停用词呢？比如过滤掉一些量词";
        LogRecod.print(NLPIRUtil.ikFenci(content));
        LogRecod.print(NLPIRUtil.jiebaFenci(content));
        return Response.success(true);
    }

    @RequestMapping("kw")
    public Response kw() {
        try {
            List<String> kws = Files.readLines(new File(testFilePath), Charsets.UTF_8);
            BufferedWriter writer = Files.newWriter(new File(kwFilePath), Charsets.UTF_8);
            Set<String> kw = new HashSet<>();
            kws.forEach(k -> {
                try {
                    String[] tmps = k.toLowerCase().split("\t")[0].split(";");
                    kw.addAll(Lists.newArrayList(tmps));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            kw.forEach(k -> {
                try {
                    writer.write((k.startsWith(" ") ? k.substring(1) : k) + "\r\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return Response.success(true);
    }

    @RequestMapping("word_count")
    public Response fun(int start, int end, int th) {
        Map<String, Map<String, Map<String, Double>>> map = new HashMap<>();
        for (int i = start; i <= end; i++) {
            String filePath = PathConfig.LdaResultsPath + "demo" + i + "\\lda_500.twords";
            LogRecod.print(filePath);
            try {
                List<String> lines = Files.readLines(new File(filePath), Charsets.UTF_8);
                LogRecod.print(lines.size());
                for (int index = 0; index < lines.size(); index++) {
                    String line = lines.get(index);
                    LogRecod.print(line);
                    String[] kws = line.split("\t");
                    for (int m = 2; m < kws.length && m <= th + 2; m++) {
                        String[] seg = kws[m].split(" ");
                        String word = seg[0];
                        if (word.equals("assess")) {
                            int a = 1;
                        }
                        Double weight = (double) (m - 1);
                        if (map.containsKey(word)) {
                            Map<String, Map<String, Double>> demoMap = map.get(word);
                            if (demoMap.containsKey("demo" + i)) {
                                Map<String, Double> topicMap = demoMap.get("demo" + i);
                                topicMap.put("topic" + index, weight);
                            } else {
                                Map<String, Double> topicMap = new HashMap<>(1);
                                topicMap.put("topic" + index, weight);
                                demoMap.put("demo" + i, topicMap);
                            }
                        } else {
                            Map<String, Map<String, Double>> demoMap = new HashMap<>();
                            Map<String, Double> topicMap = new HashMap<>(1);
                            topicMap.put("topic" + index, weight);
                            demoMap.put("demo" + i, topicMap);
                            map.put(word, demoMap);
                        }
                    }

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Map<String, Integer> countMap = new HashMap<>();
        for (Map.Entry<String, Map<String, Map<String, Double>>> m : map.entrySet()) {
            countMap.put(m.getKey(), m.getValue().size());
        }
        Map treeMap = MapUtils.sortMapByValue(countMap, true);
        LogRecod.print(treeMap);
        Map<String, Object> result = new HashMap<>();
        result.put("detail", map);
        result.put("simplify", treeMap);
        return Response.success(result);
    }
}
