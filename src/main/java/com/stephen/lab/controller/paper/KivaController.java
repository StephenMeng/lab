package com.stephen.lab.controller.paper;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import com.google.common.base.Charsets;
import com.stephen.lab.dto.analysis.Token;
import com.stephen.lab.model.paper.Kiva;
import com.stephen.lab.model.semantic.Paper;
import com.stephen.lab.service.crawler.CrawlErrorService;
import com.stephen.lab.service.paper.KivaService;
import com.stephen.lab.util.HttpUtils;
import com.stephen.lab.util.LogRecod;
import com.stephen.lab.util.Response;
import com.stephen.lab.util.StringUtils;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.wltea.analyzer.lucene.IKAnalyzer;
import weka.core.Instances;
import weka.core.converters.CSVSaver;
import weka.core.converters.DatabaseLoader;

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

    @RequestMapping(value = "split", method = RequestMethod.GET)
    public Response tokenWord() {
        Kiva kiva = new Kiva();
        kiva.setId(1334727L);
        PageHelper.startPage(1, 1);
        Kiva result = kivaService.selectOne(kiva);
        String text = result.getTranslatedDescription();
        LogRecod.print(text);
        Properties props = new Properties();
        props.put("annotators", "tokenize,ssplit,pos, lemma");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        Annotation document = new Annotation(text);
        pipeline.annotate(document);
        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
        for (CoreMap sentence : sentences) {
            for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                //分词
                String word = token.get(CoreAnnotations.TextAnnotation.class);
                //词性标注
                String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
                //词性还原
                String lema = token.get(CoreAnnotations.LemmaAnnotation.class);
                //获取命名实体识别结果
//                String ne = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);

                LogRecod.print(word + "\t" + pos + "\t" + lema);
            }
        }

        return Response.success(result);
    }

    @RequestMapping("weka")
    public Response weka() throws Exception {
        DatabaseLoader loader = new DatabaseLoader();
        loader.setUrl("jdbc:mysql://localhost:3306/lab?characterEncoding=utf-8&useSSL=false&serverTimezone=UTC");
        loader.setUser("root");
        loader.setPassword("016611sai");
        loader.setQuery("select original_description from kiva limit 10");
        Instances data1 = loader.getDataSet();
        LogRecod.print(data1.size());
        data1.forEach(d -> LogRecod.print(d));

        if (data1.classIndex() == -1)
            data1.setClassIndex(data1.numAttributes() - 1);
//        System.out.println(data1);
        CSVSaver saver = new CSVSaver();
        saver.setInstances(data1);
        saver.setFile(new File("c:/users/Stephen/desktop/csvsaver.csv"));
        saver.writeBatch();
        return Response.success("");
    }


    @RequestMapping("tfidf")
    public  Response tfidf() throws IOException {
        List<Kiva> kivaList = kivaService.selectAll();
        List<KivaResult>kivaResults=new ArrayList<>();
        Map<String, Integer> yearWordMap = new HashMap<>();
        long cur=System.currentTimeMillis();
        kivaList.forEach(kiva -> {
            String description=kiva.getTranslatedDescription();
            if(StringUtils.isNull(description)){
                description=kiva.getOriginal_description();
            }
                List<String> words = cutwords(description);
            Map<String, Integer> keywordsMap = parseKeywordMap(words);
            for (Map.Entry<String, Integer> kwm : keywordsMap.entrySet()) {
                if(yearWordMap.containsKey(kwm.getKey())){
                    yearWordMap.put(kwm.getKey(),yearWordMap.get(kwm.getKey())+1);
                }else {
                    yearWordMap.put(kwm.getKey(),1);
                }
            }
            KivaResult kivaResult=new KivaResult(kiva);
            kivaResult.setWordMap(keywordsMap);
            kivaResults.add(kivaResult);
        });


        LogRecod.print("计算关键词在年文档出现的频率" + (System.currentTimeMillis() - cur));
        for(KivaResult r:kivaResults){
            Map<String,Double>tfidfMap=new HashMap<>();
        for (Map.Entry<String,Integer> yt : r.getWordMap().entrySet()) {
            int count=yearWordMap.get(yt.getKey());
            double weight=Math.log(yearWordMap.size() / (count + 0.0000001)) * yt.getValue();
            tfidfMap.put(yt.getKey(),weight);
        }
        r.setWordTfIdf(tfidfMap);
        }
        LogRecod.print("计算TFIDF权重：" + (System.currentTimeMillis() - cur));
        for(KivaResult r:kivaResults){
            LogRecod.print(r.getKiva().getId()+"\t"+r.getWordTfIdf());
        }
//        try {
//            FileWriter fileWriter = new FileWriter(new File("c:/users/Stephen/desktop/result-tfidf.txt"));
//            for(KivaResult r:kivaResults){
//                fileWriter.write(r.getKiva().getId() + "\t" + r.getWordMap() + "\t" + r.getWordTfIdf() + "\r\n");
//            }
//            fileWriter.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        return Response.success("");
    }

    private Map<String,Integer> parseKeywordMap(List<String> words) {
        Map<String,Integer>map=new HashMap<>();
        words.forEach(word->{
            if(map.containsKey(word)){
                map.put(word,map.get(word)+1);
            }else {
                map.put(word,1);
            }
        });
        return map;
    }

    private List<String> cutwords(String description) {
        return Arrays.asList(description.split(" "));
    }
    private void getKeywordCountMaps(Map<String, Integer> keywordsMap, List<String> keywordsList) {
        for (String keyword : keywordsList) {
            if (!keywordsMap.containsKey(keyword)) {
                keywordsMap.put(keyword, 1);
            } else {
                int count = keywordsMap.get(keyword);
                keywordsMap.put(keyword, ++count);
            }
        }
    }
}
