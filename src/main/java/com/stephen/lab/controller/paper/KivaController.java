package com.stephen.lab.controller.paper;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.io.Files;
import com.hankcs.hanlp.HanLP;
import com.stephen.lab.model.paper.Kiva;
import com.stephen.lab.model.paper.KivaSimple;
import com.stephen.lab.service.crawler.CrawlErrorService;
import com.stephen.lab.service.paper.KivaService;
import com.stephen.lab.util.*;
import com.stephen.lab.util.svm.svm_predict;
import com.stephen.lab.util.svm.SvmTrain;
import io.swagger.annotations.ApiOperation;
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

    private static Properties props;
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
    public Response textrank(@RequestParam("num") int num) throws IOException {
        List<KivaSimple> kivaList = kivaService.selectAllSimple();

        String description = kivaList.get(0).getStandardDescription();
        description = description.replaceAll("<.*?>", "");
        return Response.success(HanLP.extractKeyword(description, num));
    }

    @RequestMapping("tfidf")
    public Response tfidf() throws IOException {
        List<KivaSimple> kivaList = kivaService.selectAllSimple();
        List<KivaResult> kivaResults = new ArrayList<>();
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
            for (Map.Entry<String, Integer> kwm : keywordsMap.entrySet()) {
                if (yearWordMap.containsKey(kwm.getKey())) {
                    yearWordMap.put(kwm.getKey(), yearWordMap.get(kwm.getKey()) + 1);
                } else {
                    yearWordMap.put(kwm.getKey(), 1);
                }
            }
            KivaResult kivaResult = new KivaResult(kiva);
            kivaResult.setWordMap(keywordsMap);
            kivaResults.add(kivaResult);
        });


        LogRecod.print("计算关键词在年文档出现的频率" + (System.currentTimeMillis() - cur));
        for (KivaResult r : kivaResults) {
            Map<String, Double> tfidfMap = new TreeMap<>();
            for (Map.Entry<String, Integer> yt : r.getWordMap().entrySet()) {
                int count = yearWordMap.get(yt.getKey());
                double weight = Math.log(yearWordMap.size() / (count + 0.0000001)) * yt.getValue();
                tfidfMap.put(yt.getKey(), weight);
            }
            r.setWordTfIdf(tfidfMap);
        }
        LogRecod.print("计算TFIDF权重：" + (System.currentTimeMillis() - cur));
        for (KivaResult r : kivaResults) {
            LogRecod.print(r.getKiva().getId() + "\t" + r.getWordTfIdf());
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
        KivaResult kivaResult = kivaResults.get(0);
        kivaResult.setWordTfIdf(MapUtils.sortMapByValue(kivaResult.getWordTfIdf()));
        return Response.success(kivaResult);
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

    @RequestMapping("cutword")
    public Response cutword() {
        List<Kiva> kivaList = kivaService.selectAll();
        kivaList.forEach(kiva -> {
            KivaSimple simple = new KivaSimple(kiva);
            try {
                String des = kiva.getTranslatedDescription();
                if (StringUtils.isNull(des)) {
                    des = kiva.getOriginal_description();
                }
                List<String> ct = cutwords(des);
                simple.setStandardDescription(Joiner.on(" ").join(ct));
                kivaService.insertKivaSimple(simple);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        return Response.success("");
    }

    private List<String> splitString(String description) throws IOException {
        if (StringUtils.isNull(description)) {
            return new ArrayList<>();
        }
        return Arrays.asList(description.split(" "));
    }

    private List<String> cutwords(String description) throws IOException {
        if (StringUtils.isNull(description)) {
            return new ArrayList<>();
        }
        List<String> result = new ArrayList<>();
        //创建分词对象
        Configuration configuration = DefaultConfig.getInstance();
        configuration.setUseSmart(true);
        IKSegmenter ik = new IKSegmenter(new StringReader(description), configuration);
        Lexeme lexeme = null;
        while ((lexeme = ik.next()) != null) {
            result.add(lexeme.getLexemeText());
        }
        return result;
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
    public Response trainSVMArgs() throws IOException {
        List<KivaSimple> kivaSimples = kivaService.selectAllSimple();
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
        String tag = "business";
        File tagFile = new File("C:\\Users\\stephen\\Desktop\\svm\\bussiness.txt");
        kivaSimples.forEach(kivaSimple -> {
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
                if (kivaSimple.getTags().contains(tag)) {
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
        });
        return Response.success(map);
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

    @RequestMapping("knn")
    public Response knn() {

        return Response.success("");
    }
}
