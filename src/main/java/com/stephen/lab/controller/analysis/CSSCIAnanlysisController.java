package com.stephen.lab.controller.analysis;

import com.github.pagehelper.PageHelper;
import com.stephen.lab.constant.crawler.UrlConstant;
import com.stephen.lab.dto.analysis.Token;
import com.stephen.lab.model.semantic.Paper;
import com.stephen.lab.service.semantic.PaperService;
import com.stephen.lab.util.LogRecod;
import com.stephen.lab.util.Response;
import com.stephen.lab.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sun.security.jgss.TokenTracker;

import java.io.*;
import java.util.*;

@RestController
@RequestMapping("analysis/cssci")
public class CSSCIAnanlysisController {
    @Autowired
    private PaperService paperService;
    String filePath = "C:\\Users\\stephen\\Desktop\\tfidf.txt";

    @RequestMapping("parseKeyword")
    public Response parseKeyword() {
        if (true) {
//            return Response.success(Math.log((20 / (double)(8)))*3);
        }
        long cur = System.currentTimeMillis();
        Paper condition = new Paper();
        condition.setSource(UrlConstant.CSSCI_ID);
        PageHelper.startPage(1, 0);
        List<Paper> paperList = paperService.select(condition);
//        Map<Integer,List<Paper>>yearPapers=new HashMap<>();
        Map<String, Integer> keywordsMap = new HashMap<>();
        Map<Integer, List<Paper>> yearMap = new HashMap<>();
        paperList.forEach(paper -> {
            if (!StringUtils.isNull(paper.getKeyword())) {
                List<String> keywordsList = getKeywordList(paper.getKeyword());
                try {
                    getKeywordCountMaps(keywordsMap, keywordsList);
                    int year = Integer.parseInt(paper.getYear());
                    getYearListMap(yearMap, paper, year);

                } catch (Exception e) {
                    LogRecod.print(paper);
                }
            }
        });
        Map<Integer, List<Token>> yearTokens = new HashMap<>();
        int count = getMoreThanOneKeywordCount(keywordsMap);
        Map<String, String> result = new TreeMap<>();

        for (Map.Entry<Integer, List<Paper>> ym : yearMap.entrySet()) {
            List<Token> tokens = getOneYearTokens(ym);
            yearTokens.put(ym.getKey(), tokens);
            result.put("year num" + ym.getKey(), tokens.size() + "");
        }
        LogRecod.print("解析关键词列表和每年的map：" + (System.currentTimeMillis() - cur));
        cur = System.currentTimeMillis();
        List<Token> tokenYearFreq = new ArrayList<>();
        for (Map.Entry<String, Integer> kwm : keywordsMap.entrySet()) {
            String k = kwm.getKey();
            Token token = new Token();
            token.setWord(k);
            int freq = 0;
            for (Map.Entry<Integer, List<Token>> yt : yearTokens.entrySet()) {
                List<Token> tokens = yt.getValue();
                if (tokens.contains(token)) {
                    freq++;
                }
            }
            token.setFreq(freq);
            tokenYearFreq.add(token);
        }
        LogRecod.print("计算关键词在年文档出现的频率" + (System.currentTimeMillis() - cur));
        cur = System.currentTimeMillis();
        for (Map.Entry<Integer, List<Token>> yt : yearTokens.entrySet()) {
            List<Token> tokens = yt.getValue();
            tokens.forEach(token -> {
                int index = tokenYearFreq.indexOf(token);
                Token yearToken = tokenYearFreq.get(index);

                token.setWeight(Math.log(yearMap.size() / (double) (yearToken.getFreq() + 1)) * token.getFreq());
                if (yearToken.getWord().equals("情感分析")) {
                    LogRecod.print("年：" + yt.getKey() + "\t" + yearMap.size() + "\t" + (yearToken.getFreq() + 1) + "\t" + token.getFreq() + "\t" + token.getWeight());
                }
            });
        }
        tokenYearFreq.forEach(token -> {
            if (token.getWord().equals("情感分析")) {
                LogRecod.print(token.getWord() + "\t" + token.getFreq());
            }
        });
        LogRecod.print("计算TFIDF权重：" + (System.currentTimeMillis() - cur));
        cur = System.currentTimeMillis();
        for (Map.Entry<Integer, List<Token>> yt : yearTokens.entrySet()) {
            List<Token> tokens = yt.getValue();
            for (int i = 0; i < tokens.size(); i++) {
                Token token = tokens.get(i);
                if (token.getWord().equals("情感分析")) {
                    LogRecod.print(yt.getKey() + "\t" + token.getWord() + "\t" + token.getFreq() + "\t" + token.getWeight());
                }
            }
        }
        try {
            FileWriter fileWriter = new FileWriter(new File(filePath), true);
            for (Map.Entry<Integer, List<Token>> yt : yearTokens.entrySet()) {
                List<Token> tokens = yt.getValue();
                for (int i = 0; i < tokens.size(); i++) {
                    Token token = tokens.get(i);
                    fileWriter.write(yt.getKey() + "\t" + token.getWord() + "\t" + token.getWeight() + "\t" + token.getFreq() + "\r\n");
                    if (token.getWord().equals("情感分析")) {
                        LogRecod.print(yt.getKey() + "\t" + token.getWord() + "\t" + token.getFreq() + "\t" + token.getWeight());
                    }
                }
            }
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        LogRecod.print("输出结果:" + (System.currentTimeMillis() - cur));
        result.put("totalKeyword size", keywordsMap.size() + "");
        result.put("keywod size more than one", count + "");
        result.put("year num", yearMap.size() + "");
        return Response.success(result);
    }

    @RequestMapping("compare")
    public Response compare(Integer start, Integer end) throws IOException {
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(new File(filePath)), "utf-8")
        );
        Map<Integer, List<Token>> yearTokens = new HashMap<>();
        String line = null;
        while ((line = reader.readLine()) != null) {
            String[] items = line.split("\t");
            Integer year = Integer.parseInt(items[0]);
            String kw = items[1];
            Double weight = Double.parseDouble(items[2]);
            Integer freq = Integer.parseInt(items[3]);
            Token token = new Token();
            token.setWord(kw);
            token.setWeight(weight);
            token.setFreq(freq);
            if (yearTokens.containsKey(year)) {
                List<Token> tokens = yearTokens.get(year);
                tokens.add(token);
            } else {
                List<Token> tokenList = new ArrayList<>();
                token.add(token);
                yearTokens.put(year, tokenList);
            }

        }
        double result = computeDistance(yearTokens, start, end);
        return Response.success(result);
    }

    private double computeDistance(Map<Integer, List<Token>> yearTokens, int m, int n) {
        if (m >= n || m < 0) {
            return 0;
        }
        double result = 0;
        for (int i = m; i < n; i++) {
            List<Token> ta = yearTokens.get(i);
            result += computeDistance(ta, yearTokens.get(i + 1));
        }
        return result;
    }

    /**
     * 余弦相似性
     * @param ta
     * @param tb
     * @return
     */
    private double computeDistance(List<Token> ta, List<Token> tb) {
        double result = 0;
        double fenzi = 0;
        double pa = getPow(ta);
        double pb = getPow(tb);
        for (Token a : ta) {
            if (tb.contains(a)) {
                int index = tb.indexOf(a);
                Token b = tb.get(index);
                fenzi += a.getWeight() * b.getWeight();
            }
        }
        result = Math.pow(fenzi, 2) / (pa * pb);
        result = Math.sqrt(result);
        return result;
    }

    private double getPow(List<Token> ta) {
        double result = 0;
        for (Token t : ta) {
            result += Math.pow(t.getWeight(), 2);
        }
        return result;
    }

    private int getMoreThanOneKeywordCount(Map<String, Integer> keywordsMap) {
        int count = 0;
        for (Map.Entry<String, Integer> km : keywordsMap.entrySet()) {
            if (km.getValue() > 1) {
                count++;
            } else {
            }
        }
        return count;
    }

    private void getYearListMap(Map<Integer, List<Paper>> yearMap, Paper paper, int year) {
        if (yearMap.containsKey(year)) {
            List<Paper> yearPaperList = yearMap.get(year);
            yearPaperList.add(paper);
        } else {
            List<Paper> yearPaperList = new ArrayList<>();
            yearPaperList.add(paper);
            yearMap.put(year, yearPaperList);
        }
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

    private List<Token> getOneYearTokens(Map.Entry<Integer, List<Paper>> ym) {
        List<Paper> papers = ym.getValue();
        List<Token> tokens = new ArrayList<>();
        for (Paper paper : papers) {
            List<String> keyword = getKeywordList(paper.getKeyword());
            keyword.forEach(k -> {
                Token token = new Token();
                token.setWord(k);
                token.setFreq(1);
                int index = tokens.indexOf(token);
                if (index != -1) {
                    Token exist = tokens.get(index);
                    exist.add(token);
                } else {
                    tokens.add(token);
                }
            });
        }
        return tokens;
    }

    private List<String> getKeywordList(String keyword) {
        return Arrays.asList(keyword.split(";"));
    }
}
