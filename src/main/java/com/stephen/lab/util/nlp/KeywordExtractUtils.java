package com.stephen.lab.util.nlp;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.summary.TextRankKeyword;
import com.stephen.lab.dto.analysis.Token;
import com.stephen.lab.util.StringUtils;

import java.util.*;

/**
 * Created by stephen on 2018/3/16.
 */
public class KeywordExtractUtils {
    public static List<Token> textrank(String content, int num) {
        if (StringUtils.isNull(content) || num <= 0) {
            return new ArrayList<>();
        }
        List<Token> tokens = new ArrayList<>();
        TextRankKeyword textRankKeyword = new TextRankKeyword();
        Set<Map.Entry<String, Float>> entrySet = textRankKeyword.getTermAndRank(content, num).entrySet();
        Iterator i$ = entrySet.iterator();

        while (i$.hasNext()) {
            Map.Entry<String, Float> entry = (Map.Entry) i$.next();
            Token item = new Token(entry.getKey());
            item.setWeight(entry.getValue());
            tokens.add(item);
        }
        return tokens;
    }
}
