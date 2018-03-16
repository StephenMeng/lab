package com.stephen.lab.util.nlp;

import com.stephen.lab.model.paper.SortedClusterResult;
import com.stephen.lab.dto.analysis.Token;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by stephen on 2018/3/16.
 */
public class ClusterUtils {
    public static SortedClusterResult sortedCluster(List<Token> tokenList) {
        SortedClusterResult result = new SortedClusterResult();
        int breakPoint = -1;
        double lost = Double.MAX_VALUE;
        for (int i = 0; i < tokenList.size(); i++) {
            List<Token> before = getFromKeywordList(tokenList, i, true);
            List<Token> after = getFromKeywordList(tokenList, i, false);
            if (before.size() > 0 && after.size() > 0) {
                double bs = compute(before, before.size());
                double as = compute(after, after.size());
                if (lost >= bs + as) {
                    lost = bs + as;
                    breakPoint = i;
                }
            }
        }
        result.setLost(lost);
        result.setBreakPoint(breakPoint);
        return result;
    }

    private static double compute(List<Token> list, int size) {
        double score = 0;
        Token avg = getAvgKeywordInfo(list, size);
        for (int i = 0; i < list.size(); i++) {
            Token item = list.get(i);
            score += computeTwoItem(avg, item);
        }
        return score;
    }

    private static double computeTwoItem(Token a, Token b) {
        return Math.pow(a.getWeight() - b.getWeight(), 2);
    }

    private static Token getAvgKeywordInfo(List<Token> list, int size) {
        Token info = new Token();
        list.forEach(item -> {
            info.setWeight(info.getWeight() + item.getWeight());
        });
        info.setWeight(info.getFreq() / size);
        return info;
    }

    private static List<Token> getFromKeywordList(List<Token> tokenList, int year, boolean before) {
        List<Token> result = new ArrayList<>();
        for (int i = 0; i < tokenList.size(); i++) {
            if (i < year && before) {
                result.add(tokenList.get(i));
            }
            if (i >= year && !before) {
                result.add(tokenList.get(i));
            }
        }
        return result;
    }

}
