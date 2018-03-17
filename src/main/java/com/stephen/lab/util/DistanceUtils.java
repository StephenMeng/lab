package com.stephen.lab.util;

import com.stephen.lab.dto.analysis.Token;

import java.util.List;

/**
 * Created by stephen on 2018/3/13.
 */
public class DistanceUtils {
    /**
     * 欧式距离
     *
     * @param ta
     * @param tb
     * @return
     */
    public static double osDistance(List<Token> ta, List<Token> tb) {
        double result = 0;
        for (Token a : ta) {
            int index = tb.indexOf(a);
            if (index != -1) {
                Token b = tb.get(index);
                result += Math.pow(b.getWeight() - a.getWeight(), 2);
            } else {
                result += Math.pow(a.getWeight(), 2);
            }
        }
        for (Token b : tb) {
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
    public static double cosDistance(List<Token> ta, List<Token> tb) {
        double result = 0;
        double fenzi = 0;
        double pa = getPow(ta);
        double pb = getPow(tb);
        for (Token a : ta) {
            int index = tb.indexOf(a);
            if (index != -1) {
                Token b = tb.get(index);
                fenzi += Math.abs(a.getWeight() * b.getWeight());
            }
        }
        result = fenzi / (Math.sqrt(pa) * Math.sqrt(pb));
        return (1 / result) - 1;
    }

    private static double getPow(List<Token> ta) {
        double result = 0;
        for (Token t : ta) {
            result += Math.pow(t.getWeight(), 2);
        }
        return result;
    }

    public static double cosNumricDistance(List<Double> toCompareScore, List<Double> srcScore) {
        if (toCompareScore.size() != srcScore.size()) {
            return -1;
        }
        double result = 0;
        double fenzi = 0;
        double pa = getNumricPow(srcScore);
        double pb = getNumricPow(toCompareScore);
        for (int i = 0; i < toCompareScore.size(); i++) {
            fenzi += toCompareScore.get(i) * srcScore.get(i);
        }
        result = fenzi / (Math.sqrt(pa) * Math.sqrt(pb));
        return (1 / result) - 1;
    }

    private static double getNumricPow(List<Double> srcScore) {
        double result = 0;
        for (double t : srcScore) {
            result += Math.pow(t, 2);
        }
        return result;
    }
}
