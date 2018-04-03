package com.stephen.lab.util.nlp.lda.sample.main;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.stephen.lab.constant.paper.TagType;
import com.stephen.lab.util.LogRecod;
import com.stephen.lab.util.nlp.NLPIRUtil;
import com.stephen.lab.util.nlp.lda.sample.com.FileUtil;
import com.stephen.lab.util.nlp.lda.sample.com.Stopwords;

/**
 * Class for corpus which consists of M documents
 *
 * @author yangliu
 * @blog http://blog.csdn.net/yangliuy
 * @mail yangliuyx@gmail.com
 */

public class Documents {

    ArrayList<Document> docs;
    public Map<String, Integer> termToIndexMap;
    ArrayList<String> indexToTermMap;
    Map<String, Integer> termCountMap;

    public Documents() {
        docs = new ArrayList<Document>();
        termToIndexMap = new HashMap<String, Integer>();
        indexToTermMap = new ArrayList<String>();
        termCountMap = new HashMap<String, Integer>();
    }

    public void readDocs(String docsPath) {
        for (File docFile : new File(docsPath).listFiles()) {
            Document doc = new Document(docFile.getAbsolutePath(), termToIndexMap, indexToTermMap, termCountMap);
            docs.add(doc);
        }
    }

    public void readDocs(List<String> strings) {
        for (int i = 0; i < strings.size(); i++) {
            List<String> words = NLPIRUtil.split(strings.get(i), TagType.TAG_SPLITTER);
            Document doc = new Document(i, words, termToIndexMap, indexToTermMap, termCountMap);
            docs.add(doc);
        }
    }

    public void readDocs(List<String> contents, String splitter) {
        for (int i = 0; i < contents.size(); i++) {
            List<String> words;
            if (splitter.equals("")) {
//                words = NLPIRUtil.cutwords(contents.get(i));
                words=Lists.newArrayList(contents.get(i).split(" "));
                words=NLPIRUtil.removeStopwords(words);

            } else {
                words = Lists.newArrayList(contents.get(i).split(splitter));
            }
            words = words.stream().map(String::toLowerCase).collect(Collectors.toList());
            LogRecod.print(words);
            Document doc = new Document(i, words, termToIndexMap, indexToTermMap, termCountMap);
            docs.add(doc);
        }
    }

    public static class Document {
        private String docName;
        int[] docWords;

        public Document(String docName, Map<String, Integer> termToIndexMap, ArrayList<String> indexToTermMap, Map<String, Integer> termCountMap) {
            this.docName = docName;
            //Read file and initialize word index array
            ArrayList<String> docLines = new ArrayList<String>();
            ArrayList<String> words = new ArrayList<String>();
            FileUtil.readLines(docName, docLines);
            for (String line : docLines) {
                FileUtil.tokenizeAndLowerCase(line, words);
            }
            //Remove stop words and noise words
            for (int i = 0; i < words.size(); i++) {
                if (Stopwords.isStopword(words.get(i)) || isNoiseWord(words.get(i))) {
                    words.remove(i);
                    i--;
                }
            }
            //Transfer word to index
            this.docWords = new int[words.size()];
            for (int i = 0; i < words.size(); i++) {
                String word = words.get(i);
                if (!termToIndexMap.containsKey(word)) {
                    int newIndex = termToIndexMap.size();
                    termToIndexMap.put(word, newIndex);
                    indexToTermMap.add(word);
                    termCountMap.put(word, new Integer(1));
                    docWords[i] = newIndex;
                } else {
                    docWords[i] = termToIndexMap.get(word);
                    termCountMap.put(word, termCountMap.get(word) + 1);
                }
            }
            words.clear();
        }

        public Document(int index, List<String> words, Map<String, Integer> termToIndexMap, ArrayList<String> indexToTermMap, Map<String, Integer> termCountMap) {
            this.docName = index + "";
            //Read file and initialize word index array
            ArrayList<String> docLines = new ArrayList<String>();
            //Remove stop words and noise words
            for (int i = 0; i < words.size(); i++) {
                if (Stopwords.isStopword(words.get(i)) || isNoiseWord(words.get(i))) {
                    words.remove(i);
                    i--;
                }
            }
            //Transfer word to index
            this.docWords = new int[words.size()];
            for (int i = 0; i < words.size(); i++) {
                String word = words.get(i);
                if (!termToIndexMap.containsKey(word)) {
                    int newIndex = termToIndexMap.size();
                    termToIndexMap.put(word, newIndex);
                    indexToTermMap.add(word);
                    termCountMap.put(word, new Integer(1));
                    docWords[i] = newIndex;
                } else {
                    docWords[i] = termToIndexMap.get(word);
                    termCountMap.put(word, termCountMap.get(word) + 1);
                }
            }
            words.clear();
        }

        public boolean isNoiseWord(String string) {
            // TODO Auto-generated method stub
            string = string.toLowerCase().trim();
            Pattern MY_PATTERN = Pattern.compile(".*[a-zA-Z]+.*");
            Matcher m = MY_PATTERN.matcher(string);
            // filter @xxx and URL
            if (string.matches(".*www\\..*") || string.matches(".*\\.com.*") ||
                    string.matches(".*http:.*"))
                return true;
            if (!m.matches()) {
                return true;
            } else
                return false;
        }

        public String getDocName() {
            return docName;
        }
    }
}
