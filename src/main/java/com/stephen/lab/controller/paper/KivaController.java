package com.stephen.lab.controller.paper;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import com.google.common.base.Charsets;
import com.stephen.lab.model.paper.Kiva;
import com.stephen.lab.service.crawler.CrawlErrorService;
import com.stephen.lab.service.paper.KivaService;
import com.stephen.lab.util.HttpUtils;
import com.stephen.lab.util.LogRecod;
import com.stephen.lab.util.Response;
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

import java.io.*;
import java.util.List;
import java.util.Properties;
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
}
