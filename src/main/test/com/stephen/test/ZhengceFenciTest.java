package com.stephen.test;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.io.Files;
import com.stephen.lab.Application;
import com.stephen.lab.util.LogRecod;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.wltea.analyzer.lucene.IKAnalyzer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(SpringRunner.class)
//@SpringBootTest(classes = Application.class)
@WebAppConfiguration
public class ZhengceFenciTest {
    @Test
    public void testAreaFenci() throws IOException {
        String dirPath = "C:\\Users\\Stephen\\Desktop\\兆韬\\特色小镇政策文本\\txt";
        String outPutPath = "C:\\Users\\Stephen\\Desktop\\兆韬\\特色小镇政策文本\\area";
        for (File file : new File(dirPath).listFiles()) {
            List<String> lines = Files.readLines(file, Charsets.UTF_8);
            String text = Joiner.on(" ").join(lines);
            LogRecod.print(text);
            List<String> tokens = cutword(text);
            Map<String, Integer> tokeCountMap = countMap(tokens);
            LogRecod.print(tokeCountMap);
            String area = file.getName().substring(0, file.getName().indexOf("-"));
            File of = new File(outPutPath + "\\" + area + ".txt");
            BufferedWriter writer = Files.newWriter(of, Charsets.UTF_8);
            for (Map.Entry<String, Integer> m : tokeCountMap.entrySet()) {
                writer.write(m.getKey() + "\t" + m.getValue() + "\r\n");
            }
            writer.close();
        }

    }

    @Test
    public void testDateFenci() throws IOException {
        String dirPath = "C:\\Users\\Stephen\\Desktop\\兆韬\\特色小镇政策文本\\txt";
        String outPutPath = "C:\\Users\\Stephen\\Desktop\\兆韬\\特色小镇政策文本\\date";
        for (File file : new File(dirPath).listFiles()) {
            List<String> lines = Files.readLines(file, Charsets.UTF_8);
            String text = Joiner.on(" ").join(lines);
            LogRecod.print(text);
            List<String> tokens = cutword(text);
            Map<String, Integer> tokeCountMap = countMap(tokens);
            LogRecod.print(tokeCountMap);
            String date = file.getName().substring(file.getName().indexOf("-") + 1, file.getName().lastIndexOf("-"));
            File of = new File(outPutPath + "\\" + date + ".txt");
            BufferedWriter writer = Files.newWriter(of, Charsets.UTF_8);
            for (Map.Entry<String, Integer> m : tokeCountMap.entrySet()) {
                writer.write(m.getKey() + "\t" + m.getValue() + "\r\n");
            }
            writer.close();
        }

    }

    @Test
    public void testAllDocFenci() throws IOException {
        String dirPath = "C:\\Users\\Stephen\\Desktop\\兆韬\\特色小镇政策文本\\txt";
        String outPutPath = "C:\\Users\\Stephen\\Desktop\\兆韬\\特色小镇政策文本";
        Map<String, Integer> result = new HashMap<>();
        for (File file : new File(dirPath).listFiles()) {
            List<String> lines = Files.readLines(file, Charsets.UTF_8);
            String text = Joiner.on(" ").join(lines);
            LogRecod.print(text);
            List<String> tokens = cutword(text);
            Map<String, Integer> tokeCountMap = countMap(tokens);
            for (Map.Entry<String, Integer> m : tokeCountMap.entrySet()) {
                if (result.containsKey(m.getKey())) {
                    result.put(m.getKey(), result.get(m.getKey()) + m.getValue());
                } else {
                    result.put(m.getKey(), m.getValue());
                }
            }
        }
        File of = new File(outPutPath + "\\" + "all.txt");
        BufferedWriter writer = Files.newWriter(of, Charsets.UTF_8);
        for (Map.Entry<String, Integer> m : result.entrySet()) {
            writer.write(m.getKey() + "\t" + m.getValue() + "\r\n");
        }
        writer.close();

    }

    @Test
    public void testQuyuDocFenci() throws IOException {
        Map<String, String> proAreaMap = new HashMap<>();
        proAreaMap.put("财政部", "中央");
        proAreaMap.put("发改委", "中央");
        proAreaMap.put("辽宁", "东部");
        proAreaMap.put("天津", "东部");
        proAreaMap.put("河北", "东部");
        proAreaMap.put("山东", "东部");
        proAreaMap.put("江苏", "东部");
        proAreaMap.put("上海", "东部");
        proAreaMap.put("浙江", "东部");
        proAreaMap.put("福建", "东部");
        proAreaMap.put("吉林", "中部");
        proAreaMap.put("黑龙江", "中部");
        proAreaMap.put("安徽", "中部");
        proAreaMap.put("江西", "中部");
        proAreaMap.put("湖北", "中部");
        proAreaMap.put("陕西", "西部");
        proAreaMap.put("甘肃", "西部");
        proAreaMap.put("内蒙古", "西部");
        proAreaMap.put("宁夏", "西部");
        proAreaMap.put("重庆", "西部");
        proAreaMap.put("广西", "西部");
        proAreaMap.put("云南", "西部");

        String dirPath = "C:\\Users\\Stephen\\Desktop\\兆韬\\特色小镇政策文本\\txt";
        String outPutPath = "C:\\Users\\Stephen\\Desktop\\兆韬\\特色小镇政策文本";
        Map<String, String> textMap = new HashMap<>();
        for (File file : new File(dirPath).listFiles()) {
            List<String> lines = Files.readLines(file, Charsets.UTF_8);
            String text = Joiner.on(" ").join(lines);
            String area = file.getName().substring(0, file.getName().indexOf("-"));
            String quyu = proAreaMap.get(area);
            if (textMap.containsKey(quyu)) {
                textMap.put(quyu, textMap.get(quyu) + " " + text);
            } else {
                textMap.put(quyu, text);
            }
        }
        for (Map.Entry<String, String> m : textMap.entrySet()) {
            List<String> tokens = cutword(m.getValue());
            Map<String, Integer> tokeCountMap = countMap(tokens);
            File of = new File(outPutPath + "\\quyu\\" + m.getKey() + ".txt");
            BufferedWriter writer = Files.newWriter(of, Charsets.UTF_8);
            for (Map.Entry<String, Integer> r : tokeCountMap.entrySet()) {
                writer.write(r.getKey() + "\t" + r.getValue() + "\r\n");
            }
            writer.close();
        }
    }

    private Map<String, Integer> countMap(List<String> tokens) {
        Map<String, Integer> tokeCountMap = new HashMap<>();
        for (String token : tokens) {
            if (tokeCountMap.containsKey(token)) {
                tokeCountMap.put(token, tokeCountMap.get(token) + 1);
            } else {
                tokeCountMap.put(token, 1);
            }
        }
        return tokeCountMap;
    }

    private List<String> cutword(String text) throws IOException {
        Analyzer anal = new IKAnalyzer(true);
        List<String> reslt = new ArrayList<>();
        StringReader reader = new StringReader(text);
        //分词
        TokenStream ts = anal.tokenStream("", reader);
        ts.reset();
        CharTermAttribute term = ts.getAttribute(CharTermAttribute.class);
        //遍历分词数据
        while (ts.incrementToken()) {
            reslt.add(term.toString());
        }
        reader.close();
        return reslt;
    }
}
