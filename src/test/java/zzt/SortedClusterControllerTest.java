package zzt;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.io.Files;
import com.stephen.lab.dto.analysis.Token;
import com.stephen.lab.model.paper.SortedClusterResult;
import com.stephen.lab.util.LogRecod;
import com.stephen.lab.util.MapUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(SpringJUnit4ClassRunner.class)
//@SpringBootTest(classes = Application.class)
@WebAppConfiguration
public class SortedClusterControllerTest {
    private String filePath = "C:\\Users\\Stephen\\Desktop\\兆韬\\sorted\\data.txt";
    private String outDistanceFilePath = "C:\\Users\\Stephen\\Desktop\\兆韬\\sorted\\distance.txt";

    @Test
    public void testUser() {
        try {
            List<String> lines = Files.readLines(new File(filePath), Charsets.UTF_8);
            Map<Integer, List<Token>> tokenMap = new HashMap<>();
            for (String line : lines) {
                String[] items = line.split("\t");
                List<Token> tokenList = new ArrayList<>();
                for (int i = 1; i < items.length; i++) {
                    Token token = new Token();
                    token.setWord("topic" + i);
                    token.setWeight(Double.parseDouble(items[i].trim()));
                    tokenList.add(token);
                }
                tokenMap.put(Integer.parseInt(items[0]), tokenList);
            }
            tokenMap = MapUtils.sortMapByKey(tokenMap);

            LogRecod.print(tokenMap);
//            SortedClusterResult result = sortedCluster(tokenMap);
            SortedClusterResult result = split(tokenMap, 6, 2005, 2017);

            LogRecod.print(result);
            if (true) {
                return;
            }
            BufferedWriter writer = Files.newWriter(new File(outDistanceFilePath), Charsets.UTF_8);
            for (int start = 1992; start < 2018; start++) {
                for (int end = 1992; end < 2018; end++) {

                    if (end <= start) {
                        writer.write(" " + "\t");
                        continue;
                    }
                    Map<Integer, List<Token>> tmpMap = new HashMap<>();

                    for (int i = start; i <= end; i++) {
                        tmpMap.put(i, tokenMap.get(i));
                    }
                    double score = compute(tmpMap);
                    writer.write(score + "\t");
                    if (end == 2017) {
                        LogRecod.print(start + "\t" + end + "\t" + score);
                    }
                }
                writer.write("\r\n");
            }
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static SortedClusterResult sortedCluster(Map<Integer, List<Token>> tokenMap) {
        SortedClusterResult result = new SortedClusterResult();
        int breakPoint = -1;
        double lost = Double.MAX_VALUE;
        for (Map.Entry<Integer, List<Token>> entry : tokenMap.entrySet()) {
            Map<Integer, List<Token>> before = getFromKeywordList(tokenMap, 1992, entry.getKey());
            Map<Integer, List<Token>> after = getFromKeywordList(tokenMap, entry.getKey(), 2018);
            if (before.size() > 0 && after.size() > 0) {
                double bs = compute(before);
                double as = compute(after);
                if (lost >= bs + as) {
                    lost = bs + as;
                    breakPoint = entry.getKey();
                }
            }
        }
        result.setLost(lost);
        result.setBreakPoint(breakPoint);
        return result;
    }

    private SortedClusterResult split(Map<Integer, List<Token>> tokenMap, int size, int start, int end) {

        SortedClusterResult result = new SortedClusterResult();
        if (size == 1) {
            result.setBreakPoints(null);
            if (end == start) {
                result.setLost(0);
            } else {
                Map<Integer, List<Token>> after = getFromKeywordList(tokenMap, start, end);
                double score = compute(after);
                result.setLost(score);
            }
            return result;
        }

        if (end - start + 1 == size) {
            List<Integer> bs = new ArrayList<>();
            for (int i = start; i < end; i++) {
                bs.add(i);
            }
            result.setLost(0);
            result.setBreakPoints(bs);
            return result;
        }
        double lost = Double.MAX_VALUE;
        result.setLost(lost);
        if (end - start + 1 < size) {
            LogRecod.print("size bigger than map");
            return result;
        }
        double min = Double.MAX_VALUE;
        for (int year = end - size + 1; year >= start; year--) {
            Map<Integer, List<Token>> before = getFromKeywordList(tokenMap, start, year);
            double score = compute(before);
            SortedClusterResult tmp = split(tokenMap, size - 1, year + 1, end);
            if (min > score + tmp.getLost()) {
                List<Integer> bps = new ArrayList<>();
                bps.add(year);
                if (tmp.getBreakPoints() != null) {
                    bps.addAll(tmp.getBreakPoints());
                }
                min = score + tmp.getLost();
                result.setBreakPoints(bps);
                result.setLost(min);
                LogRecod.print(year + "\t" + (tmp.getBreakPoints() != null ? Joiner.on(";").join(tmp.getBreakPoints()) : "null") + "\t" + score + "\t" + tmp.getLost());
            }
        }
        return result;
    }

    private static double compute(Map<Integer, List<Token>> map) {
        double score = 0;
        List<Token> avg = getAvgKeywordInfo(map);
        for (Map.Entry<Integer, List<Token>> entry : map.entrySet()) {
            List<Token> item = entry.getValue();
            score += osDistance(avg, item);
        }
        return score;
    }

    private static double osDistance(List<Token> as, List<Token> bs) {
        double score = 0;
        for (int i = 0; i < as.size(); i++) {
            score += Math.pow(as.get(i).getWeight() - bs.get(i).getWeight(), 2);
        }
        return Math.sqrt(score);
    }

    private static double cosDistance(List<Token> as, List<Token> bs) {
        double score = 0;
        double fenzi = 0;
        for (int i = 0; i < as.size(); i++) {
            fenzi += as.get(i).getWeight() * bs.get(i).getWeight();
        }
        double fenmu = 0;
        for (int i = 0; i < as.size(); i++) {
            fenmu *= Math.sqrt(Math.pow(as.get(i).getWeight(), 2) + Math.pow(bs.get(i).getWeight(), 2));
        }
        return fenmu / fenzi;
    }

    private static List<Token> getAvgKeywordInfo(Map<Integer, List<Token>> map) {

        List<Token> info = new ArrayList<>();
        for (Map.Entry<Integer, List<Token>> entry : map.entrySet()) {

            List<Token> tokenList = entry.getValue();
            if (info.size() == 0) {
                for (Token tmp : tokenList) {
                    Token token = new Token();
                    token.setWord(tmp.getWord());
                    token.setWeight(tmp.getWeight());
                    info.add(token);
                }
            } else {
                for (int i = 0; i < tokenList.size(); i++) {
                    Token tmp = tokenList.get(i);
                    Token token = info.get(i);
                    token.setWeight(token.getWeight() + tmp.getWeight());
                }
            }
        }
        info.forEach(item -> item.setWeight(item.getWeight() / map.size()));
        return info;
    }

    private static Map<Integer, List<Token>> getFromKeywordList(Map<Integer, List<Token>> tokenMap, int start,
                                                                int end) {
        Map<Integer, List<Token>> result = new HashMap<>();
        for (Map.Entry<Integer, List<Token>> entry : tokenMap.entrySet()) {
            if (entry.getKey() <= end && entry.getKey() >= start) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }
}
