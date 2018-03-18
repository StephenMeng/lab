package com.stephen.test;

import com.stephen.lab.Application;
import com.stephen.lab.model.paper.KeywordInfo;
import com.stephen.lab.model.paper.YearSortedClusterResult;
import com.stephen.lab.util.LogRecod;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class SortClusterTest {
    @Test
    public void sortTest() {
        List<KeywordInfo> keywordInfoList = new ArrayList<>();
        for (int i = 1; i < 5; i++) {
            KeywordInfo keywordInfo = new KeywordInfo();
            keywordInfo.setFreq(i);
            keywordInfo.setYear(i);
            keywordInfoList.add(keywordInfo);
        }
        YearSortedClusterResult result = getWordResult(keywordInfoList);
        LogRecod.print(result);
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
}
