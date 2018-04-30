package com.stephen.lab.util.nlp;

import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.huaban.analysis.jieba.JiebaSegmenter;
import com.huaban.analysis.jieba.SegToken;
import com.stephen.lab.util.StringUtils;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import org.wltea.analyzer.cfg.Configuration;
import org.wltea.analyzer.cfg.DefaultConfig;
import org.wltea.analyzer.core.IKSegmenter;
import org.wltea.analyzer.core.Lexeme;
import org.wltea.analyzer.dic.Dictionary;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;

/**
 * Created by stephen on 2018/3/17.
 */
public class NLPIRUtil {
    private static List<String> stopwordList;
    private static Properties props = new Properties();
    private static StanfordCoreNLP pipeline;    // 依次处理
    private static String stopWordPath = "C:\\Users\\Stephen\\Desktop\\svm\\stopword.txt";
    private static JiebaSegmenter segmenter = new JiebaSegmenter();
    private static Set<String> kw = new HashSet<>();

    static {
        Configuration cfg = DefaultConfig.getInstance();
        System.out.println(cfg.getMainDictionary()); // 系统默认词库
        System.out.println(cfg.getQuantifierDicionary());
        org.wltea.analyzer.dic.Dictionary.initial(cfg);
        Dictionary dic = Dictionary.getSingleton();
        dic.addWords(getDict());

        try {
            List<String> ks = Files.readLines(new File("C:\\Users\\Stephen\\Desktop\\lda\\kw.txt"), Charsets.UTF_8);
            ks.forEach(k -> {
                        try {
                            List<String> tmps = getAllWordComposition(k);
                            kw.addAll(tmps);
                        } catch (Exception e) {
                    e.printStackTrace();
                        }
                    }
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static List<String> getAllWordComposition(String k) {
        if (!k.contains(" ")) {
            return Lists.newArrayList(k);
        }
        List<String> tmp = Lists.newArrayList(k.split(" "));
        List<String> result = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        for (String s : tmp) {
            sb.append(s).append(" ");
            result.add(sb.toString().trim());
        }
        return result;
    }

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

    public static List<String> jiebaFenci(String docContent) {
        if (true) {
            return segmenter.sentenceProcess(docContent);
        }
        List<String> result = new ArrayList<>();
        List<SegToken> tokens = segmenter.process(docContent, JiebaSegmenter.SegMode.INDEX);
        tokens.forEach(token -> {
            result.add(token.word);
        });
        return result;
    }

    public static List<String> ikFenci(String docContent) {
        List<String> list = new ArrayList<>();
        StringReader input = new StringReader(docContent);
        IKSegmenter ikSeg = new IKSegmenter(input, true);   // true 用智能分词 ，false细粒度
        try {
            for (Lexeme lexeme = ikSeg.next(); lexeme != null; lexeme = ikSeg.next()) {
                list.add(lexeme.getLexemeText());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static List<String> costumFenci(String docContent) {
        List<String> tmp = ikFenci(docContent);
        List<String> result = new ArrayList<>();
        int size = tmp.size();
        StringBuilder c = new StringBuilder();
        String p = null;
        boolean pre = false;

        for (int i = 0; i < size; i++) {
            if (pre) {
                c.append(" ").append(tmp.get(i).toLowerCase());
            } else {
                c.append(tmp.get(i).toLowerCase());
            }
            if (!kw.contains(c.toString())) {
                if (pre) {
                    result.add(p);
                    i--;
                } else {
                    result.add(c.toString());
                }
                c = new StringBuilder();
                p = null;
                pre = false;
            } else {
                pre = true;
                p = c.toString();
            }
        }
        return result;
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

    public static boolean cutwords(String docContent, List<String> filterList) {
        if (StringUtils.isNull(docContent)) {
            return false;
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
                String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
                if (filterList.contains(pos)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static List<String> removeStopwords(List<String> words) {
        List<String> result = new ArrayList<>();
        words.forEach(word -> {
                    word = word.replaceAll("[\\pP‘’“”]", "");

                    if (!StringUtils.isNull(word) && !stopwordList.contains(word)) {
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

    private static List<String> getDict() {
        try {
            return Files.readLines(new File("C:\\Users\\stephen\\" +
                    "IdeaProjects\\lab\\src\\main\\resources\\dict.dic"), Charsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
