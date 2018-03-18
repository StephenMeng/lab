package com.stephen.lab.util.nlp;

import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.stephen.lab.util.StringUtils;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created by stephen on 2018/3/17.
 */
public class NLPIRUtil {
    private static List<String> stopwordList;
    private static Properties props = new Properties();
    private static StanfordCoreNLP pipeline;    // 依次处理
    private static String stopWordPath = "C:\\Users\\Stephen\\Desktop\\svm\\stopword.txt";

    static {
        try {
            stopwordList = getStopwords();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 七种Annotators
        props.put("annotators", "tokenize, ssplit, pos, lemma,  parse");
        pipeline = new StanfordCoreNLP(props);
    }

    public static List<String> getStopwords() throws IOException {
        return Files.readLines(new File(stopWordPath), Charsets.UTF_8);
    }

    public static List<String> cutwords(String docContent) {
        if (StringUtils.isNull(docContent)) {
            return new ArrayList<>();
        }
        List<String> result = new ArrayList<>();
        // 利用text创建一个空的Annotation
        Annotation document = new Annotation(docContent);
        // 对text执行所有的Annotators（七种）
        pipeline.annotate(document);

        // 下面的sentences 中包含了所有分析结果，遍历即可获知结果。
        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);

        for (CoreMap sentence : sentences) {
            for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
//                String word = token.get(CoreAnnotations.TextAnnotation.class);            // 获取分词
                // 获取词性标注
                String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
//                String ne = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);    // 获取命名实体识别结果
                // 获取词形还原结果
                String lemma = token.get(CoreAnnotations.LemmaAnnotation.class);
//                if (!pos.startsWith("V")) {
                    result.add(lemma);
//                }
            }
        }
        return result;
    }

    public static List<String> removeStopwords(List<String> words) {
        List<String> result = new ArrayList<>();
        words.forEach(word -> {
                    if (!stopwordList.contains(word)) {
                        result.add(word);
                    }
                }
        );
        return result;
    }

    public static List<String> split(String docContent, String spliter) {
        List<String> result = Lists.newArrayList(Splitter.on(spliter).trimResults().split(docContent));
        result.removeIf(StringUtils::isNull);
        return result;
    }
}