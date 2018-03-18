package com.stephen.lab.controller.paper;

import com.github.pagehelper.PageHelper;
import com.stephen.lab.constant.crawler.UrlConstant;
import com.stephen.lab.dto.analysis.FreqToken;
import com.stephen.lab.model.paper.KeywordInfo;
import com.stephen.lab.model.paper.LostPoint;
import com.stephen.lab.model.paper.YearSortedClusterResult;
import com.stephen.lab.model.semantic.Paper;
import com.stephen.lab.service.semantic.PaperService;
import com.stephen.lab.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.util.*;

import static com.stephen.lab.constant.StaticResource.distanceList;
import static com.stephen.lab.constant.StaticResource.pointList;

@RestController
@RequestMapping(value = "analysis/cssci")
public class CssciAnanlysisController {
    @Autowired
    private PaperService paperService;
    String tfidfResultPath = "C:\\Users\\Stephen\\Desktop\\tfidf.txt";
    String keywordFreqPath = "C:\\Users\\Stephen\\Desktop\\freq.txt";
    String distancePath = "C:\\Users\\Stephen\\Desktop\\result-distance.txt";
    String lostPath = "C:\\Users\\Stephen\\Desktop\\result-lost.txt";
    String nodePath = "C:\\Users\\Stephen\\Desktop\\result-node.txt";
    String keywordPath = "C:\\Users\\Stephen\\Desktop\\result-keyword.txt";


    @RequestMapping(value = "parseKeyword", method = RequestMethod.GET)
    public Response parseKeyword() {
        long cur = System.currentTimeMillis();
        Paper condition = new Paper();
        condition.setSource(UrlConstant.CSSCI_ID);
        PageHelper.startPage(1, 0);
        List<Paper> paperList = paperService.select(condition);

        Map<String, Integer> keywordsMap = new HashMap<>();
        Map<Integer, List<Paper>> yearMap = new HashMap<>();
        paperList.forEach(paper -> {
            if (!StringUtils.isNull(paper.getKeyword()) && !"2017".equals(paper.getYear())) {
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

        Map<Integer, List<FreqToken>> yearFreqTokens = new HashMap<>();
        int count = getMoreThanOneKeywordCount(keywordsMap);
        Map<String, String> result = new TreeMap<>();

        for (Map.Entry<Integer, List<Paper>> ym : yearMap.entrySet()) {
            List<FreqToken> FreqTokens = getOneYearFreqTokens(ym);
            yearFreqTokens.put(ym.getKey(), FreqTokens);
            result.put("year num" + ym.getKey(), FreqTokens.size() + "");
        }
        LogRecod.print("解析关键词列表和每年的map：" + (System.currentTimeMillis() - cur));
        cur = System.currentTimeMillis();
        List<FreqToken> FreqTokenYearFreq = new ArrayList<>();
        for (Map.Entry<String, Integer> kwm : keywordsMap.entrySet()) {
            String k = kwm.getKey();
            if (kwm.getValue() > 1) {
                FreqToken FreqToken = new FreqToken();
                FreqToken.setWord(k);
                int freq = 0;
                for (Map.Entry<Integer, List<FreqToken>> yt : yearFreqTokens.entrySet()) {
                    List<FreqToken> FreqTokens = yt.getValue();
                    if (FreqTokens.contains(FreqToken)) {
                        freq++;
                    }
                }
                FreqToken.setFreq(freq);
                FreqTokenYearFreq.add(FreqToken);
            }
        }
        LogRecod.print("计算关键词在年文档出现的频率" + (System.currentTimeMillis() - cur));
        cur = System.currentTimeMillis();
        for (Map.Entry<Integer, List<FreqToken>> yt : yearFreqTokens.entrySet()) {
            List<FreqToken> FreqTokens = yt.getValue();
            FreqTokens.forEach(FreqToken -> {
                int index = FreqTokenYearFreq.indexOf(FreqToken);
                if (index != -1) {
                    FreqToken yearFreqToken = FreqTokenYearFreq.get(index);

                    FreqToken.setWeight(Math.log(yearMap.size() / (yearFreqToken.getFreq() + 0.0000001)) * FreqToken.getFreq());
                    if ("情感分析".equals(yearFreqToken.getWord())) {
                        LogRecod.print("年：" + yt.getKey() + "\t" + yearMap.size() + "\t" + (yearFreqToken.getFreq() + 1) + "\t" + FreqToken.getFreq() + "\t" + FreqToken.getWeight());
                    }
                }
            });
        }
        LogRecod.print("计算TFIDF权重：" + (System.currentTimeMillis() - cur));
        cur = System.currentTimeMillis();
        for (Map.Entry<Integer, List<FreqToken>> yt : yearFreqTokens.entrySet()) {
            List<FreqToken> FreqTokens = yt.getValue();
            for (int i = 0; i < FreqTokens.size(); i++) {
                FreqToken FreqToken = FreqTokens.get(i);
            }
        }
        try {
            FileWriter fileWriter = new FileWriter(new File(keywordFreqPath));
            for (FreqToken FreqToken : FreqTokenYearFreq) {
                fileWriter.write(FreqToken.getWord() + "\t" + FreqToken.getFreq() + "\t" + keywordsMap.get(FreqToken.getWord()) + "\r\n");
            }
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            FileWriter fileWriter = new FileWriter(new File(tfidfResultPath));
            for (Map.Entry<Integer, List<FreqToken>> yt : yearFreqTokens.entrySet()) {
                List<FreqToken> FreqTokens = yt.getValue();
                for (int i = 0; i < FreqTokens.size(); i++) {
                    FreqToken FreqToken = FreqTokens.get(i);
                    int in = FreqTokenYearFreq.indexOf(FreqToken);
                    if (in != -1) {
                        FreqToken yearFreq = FreqTokenYearFreq.get(in);
                        fileWriter.write(yt.getKey() + "\t" + FreqToken.getWord() +
                                "\t" + FreqToken.getWeight() + "\t" + FreqToken.getFreq() +
                                "\t" + yearFreq.getFreq() + "\t" + keywordsMap.get(FreqToken.getWord()) + "\r\n");
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

    @RequestMapping(value = "compare", method = RequestMethod.GET)
    public Response compare(Integer start, Integer end, Integer num, Integer type) throws IOException {
        Map<Integer, ArrayList<FreqToken>> yearFreqTokens = getYearFreqTokens();
        Map<Object, Object> r = new HashMap<>();
        if (type == 1) {
            //节点信息
            FileWriter fileWriter = new FileWriter(new File(nodePath));
            for (int i = 2; i <= end - start + 1; i++) {

                LostPoint lostPoint = classify(yearFreqTokens, start, end, i);
                fileWriter.write(lostPoint.getPoints().toString() + "\r\n");
//            r.put(i, lostPoint);
                r.put(i, lostPoint.getStart() + "\t" + lostPoint.getEnd() + "\t" + lostPoint.getLost() + "\t" + lostPoint.getPoints());
            }
            fileWriter.close();
        } else if (type == 2) {
            LostPoint lostPoint = classify(yearFreqTokens, start, end, num);
//            r.put(i, lostPoint);
            r.put(num, lostPoint.getStart() + "\t" + lostPoint.getEnd() + "\t" + lostPoint.getLost() + "\t" + lostPoint.getPoints());
        } else if (type == 3) {
            //损失数据
            FileWriter fileWriter = new FileWriter(new File(lostPath));
            for (int i = start; i <= end; i++) {
                fileWriter.write(i + "\t");
                for (int j = 2; j <= num; j++) {
                    LostPoint lostPoint = classify(yearFreqTokens, start, i, j);

                    //            r.put(i, lostPoint);
                    r.put(i + "-" + j, lostPoint);
                    if (lostPoint.getStart() != null) {
                        fileWriter.write(DoubleUtils.threeBit(lostPoint.getLost()) +
                                "(" + (lostPoint.getPoints() == null ? "" : lostPoint.getPoints().get(lostPoint.getPoints().size() - 1)) + ")" + "\t");
                    } else {
                        fileWriter.write(" " + "\t");
                    }
                }
                fileWriter.write("\r\n");
            }
            fileWriter.close();
        } else if (type == 4) {
            //计算每两年之间的的距离
            FileWriter fileWriter = new FileWriter(new File(distancePath));
            for (int i = start; i <= end; i++) {
                fileWriter.write(i + "\t");
            }
            fileWriter.write("\r\n");
            for (int i = start; i <= end; i++) {
                fileWriter.write(i + "\t");
                for (int j = start; j <= i; j++) {
                    double re = computeDistance(yearFreqTokens, j, i);
                    fileWriter.write(DoubleUtils.threeBit(re) + "\t");
                }
                fileWriter.write("\r\n");
            }
            fileWriter.close();
        } else if (type == 5) {
            double year = computeDistance(yearFreqTokens, start, end);
//            r.put(i, lostPoint);
            r.put(num, year);
        } else {
            LostPoint lostPoint = classify(yearFreqTokens, start, end, num);
            r.put(num, lostPoint);
        }
//        double result = computeDistance(yearFreqTokens, start, end);
        return Response.success(r);
    }

    @RequestMapping(value = "single_keyword", method = RequestMethod.GET)
    public Response single() throws IOException {
        Map<Integer, ArrayList<FreqToken>> yearFreqTokens = getYearFreqTokens();

        List<FreqToken> FreqTokenList = getFreqTokenList();
        List<YearSortedClusterResult> resultList = new ArrayList<>();
        long cur = System.currentTimeMillis();
        for (int i = 1; i < FreqTokenList.size(); i++) {
            FreqToken FreqToken = FreqTokenList.get(i);
            List<KeywordInfo> keywordInfoList = new ArrayList<>();

//            if (FreqToken.getWord().equals("LDA")) {
            for (Map.Entry<Integer, ArrayList<FreqToken>> map : yearFreqTokens.entrySet()) {
//                if (map.getKey() > 2012) {
                KeywordInfo info = new KeywordInfo();
                int year = map.getKey();
                info.setYear(year);
                List<FreqToken> FreqTokens = map.getValue();
                int index = FreqTokens.indexOf(FreqToken);
                if (index == -1) {
                    info.setFreq(0);
                } else {
                    info.setFreq(FreqTokens.get(index).getFreq());
                }
                keywordInfoList.add(info);
//                }
            }
            int totalFreq = getFreqCount(keywordInfoList);
            YearSortedClusterResult result = getWordResult(keywordInfoList);
            result.setWord(FreqToken.getWord());
            result.setFreq(totalFreq);
            resultList.add(result);
//            }
            if (i % 100 == 0) {
                LogRecod.print(i + "\t" + (System.currentTimeMillis() - cur));
            }
        }
        FileWriter fileWriter = new FileWriter(new File(keywordPath));
        resultList.forEach(YearSortedClusterResult -> {
            try {
                fileWriter.write(YearSortedClusterResult.getWord() + "\t"
                        + YearSortedClusterResult.getBreakPoint() + "\t" + YearSortedClusterResult.getBefore()
                        + "\t" + YearSortedClusterResult.getAfter() + "\t" + YearSortedClusterResult.getFreq() + "\t" + YearSortedClusterResult.getScore() + "\t"
                        + YearSortedClusterResult.getLost() + "\r\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        fileWriter.close();
        return Response.success(resultList);
    }

    public void sortTest() {
        List<KeywordInfo> keywordInfoList = new ArrayList<>();
        for (int i = 1; i < 5; i++) {
            KeywordInfo keywordInfo = new KeywordInfo();
            keywordInfo.setFreq(i);
            keywordInfo.setYear(i);
            keywordInfoList.add(keywordInfo);
        }
        YearSortedClusterResult result = getWordResult(keywordInfoList);
    }

    private YearSortedClusterResult getWordResult(List<KeywordInfo> keywordInfoList) {
        YearSortedClusterResult result = new YearSortedClusterResult();
        int breakPoint = -1;
        int beforeNum = 0;
        int afterNum = 0;
        double lost = Double.MAX_VALUE;
        for (KeywordInfo keyword : keywordInfoList) {
            List<KeywordInfo> before = getFromKeywordList(keywordInfoList, keyword.getYear(), true);
            List<KeywordInfo> after = getFromKeywordList(keywordInfoList, keyword.getYear(), false);
            if (before.size() > 0 && after.size() > 0) {
                double bs = compute(before, before.size());
                double as = compute(after, after.size());
                if (lost >= bs + as) {
                    lost = bs + as;
                    breakPoint = keyword.getYear();
                    beforeNum = getFreqCount(before);
                    afterNum = getFreqCount(after);
                }
            }
        }
        result.setBefore(beforeNum);
        result.setAfter(afterNum);
        result.setLost(lost);
        result.setBreakPoint(breakPoint);
        result.setScore((afterNum - beforeNum) / (double) (beforeNum + afterNum));
        return result;
    }

    private int getFreqCount(List<KeywordInfo> before) {
        int count = 0;
        for (int i = 0; i < before.size(); i++) {
            count += before.get(i).getFreq();
        }
        return count;
    }

    private double compute(List<KeywordInfo> list, int size) {
        double score = 0;
        KeywordInfo avg = getAvgKeywordInfo(list, size);
        for (int i = 0; i < list.size(); i++) {
            KeywordInfo item = list.get(i);
            score += computeTwoItem(avg, item);
        }
//        score = Math.sqrt(score);
        return score;
    }

    private double computeTwoItem(KeywordInfo a, KeywordInfo b) {
        return Math.pow(a.getFreq() - b.getFreq(), 2);
    }

    private KeywordInfo getAvgKeywordInfo(List<KeywordInfo> list, int size) {
        KeywordInfo info = new KeywordInfo();
        list.forEach(item -> {
            info.setFreq(info.getFreq() + item.getFreq());
        });
        info.setFreq(info.getFreq() / size);
        return info;
    }

    private List<KeywordInfo> getFromKeywordList(List<KeywordInfo> keywordInfoList, int year, boolean before) {
        List<KeywordInfo> result = new ArrayList<>();
        keywordInfoList.forEach(keywordInfo -> {
            if (keywordInfo.getYear() < year && before) {
                result.add(keywordInfo);
            }
            if (keywordInfo.getYear() >= year && !before) {
                result.add(keywordInfo);
            }
        });
        return result;
    }

    private double compulateFreqTokenDistance(FreqToken a, FreqToken b) {
        int fa = a.getFreq();
        int fb = b.getFreq();
        return Math.pow(fa - fb, 2) / Math.pow(fa + fb, 2);
    }

    private void printKeywordInfo(Map<Integer, List<FreqToken>> yearFreqTokens, String word) {
        FreqToken t = new FreqToken();
        t.setWord(word);
        for (Map.Entry<Integer, List<FreqToken>> yt : yearFreqTokens.entrySet()) {
            List<FreqToken> FreqTokens = yt.getValue();
            int index = FreqTokens.indexOf(t);
            FreqToken tt = FreqTokens.get(index);
            LogRecod.print(yt.getKey() + "\t" + tt.getWord() + "\t" + tt.getFreq() + "\t" + DoubleUtils.threeBit(tt.getWeight()));

        }
    }

    public List<FreqToken> getFreqTokenList() throws IOException {
        List<FreqToken> FreqTokenList = new ArrayList<>();
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(new File(keywordFreqPath)), "utf-8")
        );
        String line;
        while ((line = reader.readLine()) != null) {
            String word = line.split("\t")[0];
            String freq = line.split("\t")[1];
            FreqToken FreqToken = new FreqToken();
            FreqToken.setWord(word);
            FreqToken.setFreq(Integer.parseInt(freq));
            FreqTokenList.add(FreqToken);
        }
        return FreqTokenList;
    }

    private Map<Integer, ArrayList<FreqToken>> getYearFreqTokens() throws IOException {
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(new File(tfidfResultPath)), "utf-8")
        );
        Map<Integer, ArrayList<FreqToken>> yearFreqTokens = new HashMap<>();
        String line;
        while ((line = reader.readLine()) != null) {
            String[] items = line.split("\t");
            Integer year = Integer.parseInt(items[0]);
            String kw = items[1];
            Double weight = Double.parseDouble(items[2]);
            Integer freq = Integer.parseInt(items[3]);
            FreqToken FreqToken = new FreqToken();
            FreqToken.setWord(kw);
            FreqToken.setWeight(weight);
            FreqToken.setFreq(freq);
            if (yearFreqTokens.containsKey(year)) {
                ArrayList<FreqToken> FreqTokens = yearFreqTokens.get(year);
                FreqTokens.add(FreqToken);
            } else {
                ArrayList<FreqToken> FreqTokenList = new ArrayList<>();
                FreqToken.add(FreqToken);
                yearFreqTokens.put(year, FreqTokenList);
            }

        }
        return yearFreqTokens;
    }

    private LostPoint classify(Map<Integer, ArrayList<FreqToken>> yearFreqTokens, Integer start, Integer end, Integer num) {
        if (end - start + 1 < num) {
            return new LostPoint();
        }
        LostPoint p = new LostPoint();
        p.setStart(start);
        p.setEnd(end);
        if (num == 1) {
            p.setLost(computeDistance(yearFreqTokens, start, end));
            p.setNum(1);
        } else if (num == 2 && end - start == 1) {
            p.setNum(2);
            p.setPoints(ListUtil.newItemsToList(end));
            p.setLost(0);
            return p;
        } else {
            List<Integer> spreate = new ArrayList<>();
            double lost = Double.MAX_VALUE;
            int lastIndex = -1;
            if (num == 2) {
                for (int i = end; i > start; i--) {
                    double result = computeDistance(yearFreqTokens, i, end);
                    double temp = computeDistance(yearFreqTokens, start, i - 1);
                    LogRecod.print(num + "\t" + start + "\t" + end + "\t" + i + "\t" + result + "\t" + temp + "\t" + lost);
                    temp += result;
                    if (lost > temp) {
                        lost = temp;
                        lastIndex = i;
                    }
                }
            } else {
                List<LostPoint> prePoints = getPrePoints(yearFreqTokens, start, end, num);
                int index = -1;
                for (int i = 0; i < prePoints.size(); i++) {
                    LostPoint pre = prePoints.get(i);
                    Integer e = pre.getEnd();
                    double temp = computeDistance(yearFreqTokens, e + 1, end);
                    LogRecod.print(num + "\t" + start + "\t" + end + "\t" + (e + 1) + "\t" + pre.getLost() + "\t" + temp + "\t" + lost);
                    temp += +pre.getLost();
                    if (lost > temp) {
                        lost = temp;
                        index = i;
                        lastIndex = e + 1;
                    }
                }
                prePoints.get(index).getPoints().forEach(point -> {
                    int in = point;
                    spreate.add(in);
                });
            }
            spreate.add(lastIndex);
            p.setPoints(spreate);
            p.setLost(lost);
            p.setNum(spreate.size() + 1);
        }
        pointList.add(p);
        return p;
    }

    private List<LostPoint> getPrePoints(Map<Integer, ArrayList<FreqToken>> yearFreqTokens, Integer startYear, Integer endYear, Integer num) {
        int start = startYear;
        int end = endYear - 1;
        int classifynum = num - 1;
        List<LostPoint> lp = new ArrayList<>();
        for (int i = end; i > start; i--) {

            LostPoint p = select(pointList, start, i, classifynum);
            if (p == null) {
                p = classify(yearFreqTokens, start, i, classifynum);
            }
            if (p.getStart() != null) {
                lp.add(p);
            }
        }
        return lp;
    }

    private LostPoint select(List<LostPoint> pointList, Integer start, Integer end, Integer num) {
        LostPoint point = null;
        for (LostPoint p : pointList) {
            if (p.getStart().equals(start) && p.getEnd().equals(end) && p.getNum().equals(num)) {
                point = p;
                break;
            }
        }
        return point;
    }

    private double computeDistance(Map<Integer, ArrayList<FreqToken>> yearFreqTokens, int m, int n) {
        if (m >= n || m < 0) {
            return 0;
        }
        double distance = getCacheDistancePoints(m, n);
        if (distance != -1) {
            return distance;
        }
        double result = 0;
        ArrayList<List<FreqToken>> listList = new ArrayList<>();
        for (int i = m; i <= n; i++) {
            List<FreqToken> ta = yearFreqTokens.get(i);
            listList.add(ta);
        }
        result = computeDistance(listList);
        LostPoint lp = new LostPoint();
        lp.setStart(m);
        lp.setEnd(n);
        lp.setLost(result);
        distanceList.add(lp);
        LogRecod.print(m + "\t" + n);
        return result;
    }

    private double getCacheDistancePoints(int m, int n) {
        for (LostPoint lp : distanceList) {
            if (lp.getStart() == m && lp.getEnd() == n) {
                return lp.getLost();
            }
        }
        return -1;
    }

    private double computeDistance(List<List<FreqToken>> listList) {
        double result = 0;
        List<FreqToken> avgFreqTokens = getAvgFreqTokens(listList);
        for (List<FreqToken> list : listList) {
            result += computeDistance(list, avgFreqTokens);
        }
        return result;
    }

    private List<FreqToken> getAvgFreqTokens(List<List<FreqToken>> listList) {
        List<FreqToken> FreqTokenList = new ArrayList<>();
        listList.forEach(list -> {
            list.forEach(item -> {
                int index = FreqTokenList.indexOf(item);
                if (index == -1) {
                    FreqToken FreqToken = new FreqToken();
                    FreqToken.setWord(item.getWord());
                    FreqToken.setFreq(item.getFreq());
                    FreqToken.setWeight(item.getWeight());
                    FreqTokenList.add(FreqToken);
                } else {
                    FreqToken FreqToken = FreqTokenList.get(index);
                    FreqToken.setFreq(FreqToken.getFreq() + item.getFreq());
                    FreqToken.setWeight(FreqToken.getWeight() + item.getWeight());
                }
            });
        });
        FreqTokenList.forEach(FreqToken -> {
                    FreqToken.setWeight(FreqToken.getWeight() / listList.size());
                }
        );
        return FreqTokenList;
    }

    /**
     * 计算列表间的距离
     *
     * @param ta
     * @param tb
     * @return
     */
    private double computeDistance(List<FreqToken> ta, List<FreqToken> tb) {
//        return osDistance(ta, tb);
        return cosDistance(ta, tb);

    }

    /**
     * 欧式距离
     *
     * @param ta
     * @param tb
     * @return
     */
    private double osDistance(List<FreqToken> ta, List<FreqToken> tb) {
        double result = 0;
        for (FreqToken a : ta) {
            int index = tb.indexOf(a);
            if (index != -1) {
                FreqToken b = tb.get(index);
                result += Math.pow(b.getWeight() - a.getWeight(), 2);
            } else {
                result += Math.pow(a.getWeight(), 2);
            }
        }
        for (FreqToken b : tb) {
            int index = ta.indexOf(b);
            if (index == -1) {
                result += Math.pow(b.getWeight(), 2);
            }
        }
        result = Math.sqrt(result);
        return result;
    }

    /**
     * 余弦相似性
     *
     * @param ta
     * @param tb
     * @return
     */
    private double cosDistance(List<FreqToken> ta, List<FreqToken> tb) {
        double result = 0;
        double fenzi = 0;
        double pa = getPow(ta);
        double pb = getPow(tb);
        for (FreqToken a : ta) {
            int index = tb.indexOf(a);
            if (index != -1) {
                FreqToken b = tb.get(index);
                fenzi += Math.abs(a.getWeight() * b.getWeight());
            }
        }
        result = fenzi / (Math.sqrt(pa) * Math.sqrt(pb));
        return (1 / result) - 1;
    }

    private double getPow(List<FreqToken> ta) {
        double result = 0;
        for (FreqToken t : ta) {
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

    private List<FreqToken> getOneYearFreqTokens(Map.Entry<Integer, List<Paper>> ym) {
        List<Paper> papers = ym.getValue();
        List<FreqToken> FreqTokens = new ArrayList<>();
        for (Paper paper : papers) {
            List<String> keyword = getKeywordList(paper.getKeyword());
            keyword.forEach(k -> {
                FreqToken FreqToken = new FreqToken();
                FreqToken.setWord(k);
                FreqToken.setFreq(1);
                int index = FreqTokens.indexOf(FreqToken);
                if (index != -1) {
                    FreqToken exist = FreqTokens.get(index);
                    exist.add(FreqToken);
                } else {
                    FreqTokens.add(FreqToken);
                }
            });
        }
        return FreqTokens;
    }

    private List<String> getKeywordList(String keyword) {
        keyword = keyword.toUpperCase();
        return Arrays.asList(keyword.split(";"));
    }


}
