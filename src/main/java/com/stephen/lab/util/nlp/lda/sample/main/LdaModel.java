package com.stephen.lab.util.nlp.lda.sample.main;

/**
 * Class for Lda model
 *
 * @author yangliu
 * @blog http://blog.csdn.net/yangliuy
 * @mail yangliuyx@gmail.com
 */

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.stephen.lab.util.LogRecod;
import com.stephen.lab.util.nlp.lda.sample.com.FileUtil;
import com.stephen.lab.util.nlp.lda.sample.conf.PathConfig;

public class LdaModel {

    int[][] doc;//word index array
    int V, K, M;//vocabulary size, topic number, document number
    int[][] z;//topic label array
    float alpha; //doc-topic dirichlet prior parameter
    float beta; //topic-word dirichlet prior parameter
    int[][] nmk;//given document m, count times of topic k. M*K
    int[][] nkt;//given topic k, count times of term t. K*V
    int[] nmkSum;//Sum for each row in nmk
    int[] nktSum;//Sum for each row in nkt
    double[][] phi;//Parameters for topic-word distribution K*V
    double[][] theta;//Parameters for doc-topic distribution M*K
    int iterations;//Times of iterations
    int saveStep;//The number of iterations between two saving
    int beginSaveIters;//Begin save model at this iteration

    public LdaModel(LdaGibbsSampling.modelparameters modelparam) {
        // TODO Auto-generated constructor stub
        alpha = modelparam.alpha;
        beta = modelparam.beta;
        iterations = modelparam.iteration;
        K = modelparam.topicNum;
        saveStep = modelparam.saveStep;
        beginSaveIters = modelparam.beginSaveIters;
    }

    public void initializeModel(Documents docSet) {
        // TODO Auto-generated method stub
        M = docSet.docs.size();
        V = docSet.termToIndexMap.size();
        nmk = new int[M][K];
        nkt = new int[K][V];
        nmkSum = new int[M];
        nktSum = new int[K];
        phi = new double[K][V];
        theta = new double[M][K];

        //initialize documents index array
        doc = new int[M][];
        for (int m = 0; m < M; m++) {
            //Notice the limit of memory
            int N = docSet.docs.get(m).docWords.length;
            doc[m] = new int[N];
            for (int n = 0; n < N; n++) {
                doc[m][n] = docSet.docs.get(m).docWords[n];
            }
        }

        //initialize topic lable z for each word
        z = new int[M][];
        for (int m = 0; m < M; m++) {
            int N = docSet.docs.get(m).docWords.length;
            z[m] = new int[N];
            for (int n = 0; n < N; n++) {
                int initTopic = (int) (Math.random() * K);// From 0 to K - 1
                z[m][n] = initTopic;
                //number of words in doc m assigned to topic initTopic add 1
                nmk[m][initTopic]++;
                //number of terms doc[m][n] assigned to topic initTopic add 1
                nkt[initTopic][doc[m][n]]++;
                // total number of words assigned to topic initTopic add 1
                nktSum[initTopic]++;
            }
            // total number of words in document m is N
            nmkSum[m] = N;
        }
    }

    public void inferenceModel(Documents docSet) throws IOException {
        // TODO Auto-generated method stub
        if (iterations < saveStep + beginSaveIters) {
            System.err.println("Error: the number of iterations should be larger than " + (saveStep + beginSaveIters));
            System.exit(0);
        }
        for (int i = 0; i < iterations; i++) {
//            System.out.println("Iteration " + i);
            if ((i >= beginSaveIters) && (((i - beginSaveIters) % saveStep) == 0)) {
                //Saving the model
                System.out.println("Saving model at iteration " + i + " ... ");
                //Firstly update parameters
                updateEstimatedParameters();
                //Secondly print model variables
//                saveIteratedModel(i, docSet);
            }

            //Use Gibbs Sampling to update z[][]
            for (int m = 0; m < M; m++) {
                int N = docSet.docs.get(m).docWords.length;
                for (int n = 0; n < N; n++) {
                    // Sample from p(z_i|z_-i, w)
                    int newTopic = sampleTopicZ(m, n);
                    z[m][n] = newTopic;
                }
            }
        }
    }

    private void updateEstimatedParameters() {
        // TODO Auto-generated method stub
        for (int k = 0; k < K; k++) {
            for (int t = 0; t < V; t++) {
                phi[k][t] = (nkt[k][t] + beta) / (nktSum[k] + V * beta);
            }
        }

        for (int m = 0; m < M; m++) {
            for (int k = 0; k < K; k++) {
                theta[m][k] = (nmk[m][k] + alpha) / (nmkSum[m] + K * alpha);
            }
        }
    }

    private int sampleTopicZ(int m, int n) {
        // TODO Auto-generated method stub
        // Sample from p(z_i|z_-i, w) using Gibbs upde rule

        //Remove topic label for w_{m,n}
        int oldTopic = z[m][n];
        nmk[m][oldTopic]--;
        nkt[oldTopic][doc[m][n]]--;
        nmkSum[m]--;
        nktSum[oldTopic]--;

        //Compute p(z_i = k|z_-i, w)
        double[] p = new double[K];
        for (int k = 0; k < K; k++) {
            p[k] = (nkt[k][doc[m][n]] + beta) / (nktSum[k] + V * beta) * (nmk[m][k] + alpha) / (nmkSum[m] + K * alpha);
        }

        //Sample a new topic label for w_{m, n} like roulette
        //Compute cumulated probability for p
        for (int k = 1; k < K; k++) {
            p[k] += p[k - 1];
        }
        double u = Math.random() * p[K - 1]; //p[] is unnormalised
        int newTopic;
        for (newTopic = 0; newTopic < K; newTopic++) {
            if (u < p[newTopic]) {
                break;
            }
        }

        //Add new topic label for w_{m, n}
        nmk[m][newTopic]++;
        nkt[newTopic][doc[m][n]]++;
        nmkSum[m]++;
        nktSum[newTopic]++;
        return newTopic;
    }

    public void saveIteratedModel(int iters, Documents docSet) throws IOException {
        // TODO Auto-generated method stub
        //lda.params lda.phi lda.theta lda.tassign lda.twords
        //lda.params
        String resPath = PathConfig.LdaResultsPath;
        String modelName = "lda_" + iters;
        ArrayList<String> lines = new ArrayList<String>();
        lines.add("alpha = " + alpha);
        lines.add("beta = " + beta);
        lines.add("topicNum = " + K);
        lines.add("docNum = " + M);
        lines.add("termNum = " + V);
        lines.add("iterations = " + iterations);
        lines.add("saveStep = " + saveStep);
        lines.add("beginSaveIters = " + beginSaveIters);
        FileUtil.writeLines(resPath + modelName + ".params", lines);

        //lda.phi K*V
        BufferedWriter writer = new BufferedWriter(new FileWriter(resPath + modelName + ".phi"));
        for (int i = 0; i < K; i++) {
            for (int j = 0; j < V; j++) {
                writer.write(docSet.indexToTermMap.get(j) + ";" + phi[i][j] + "\t");
            }
            writer.write("\n");
        }
        writer.close();

        //lda.theta M*K
        writer = new BufferedWriter(new FileWriter(resPath + modelName + ".theta"));
        for (int i = 0; i < M; i++) {
            writer.write(docSet.docs.get(i).getDocName() + "\t");
            for (int j = 0; j < K; j++) {
                writer.write(theta[i][j] + "\t");
            }
            writer.write("\n");
        }
        writer.close();

        //lda.tassign
        writer = new BufferedWriter(new FileWriter(resPath + modelName + ".tassign"));
        for (int m = 0; m < M; m++) {
            for (int n = 0; n < doc[m].length; n++) {
                writer.write(doc[m][n] + ":" + z[m][n] + "\t");
            }
            writer.write("\n");
        }
        writer.close();

        //lda.twords phi[][] K*V
        writer = new BufferedWriter(new FileWriter(resPath + modelName + ".twords"));
        int topNum = 20; //Find the top 20 topic words in each topic
        for (int i = 0; i < K; i++) {
            List<Integer> tWordsIndexArray = new ArrayList<Integer>();
            for (int j = 0; j < V; j++) {
                tWordsIndexArray.add(new Integer(j));
            }
            Collections.sort(tWordsIndexArray, new LdaModel.TwordsComparable(phi[i]));
            writer.write("topic " + i + "\t:\t");
            for (int t = 0; t < topNum; t++) {
                try {
                    writer.write(docSet.indexToTermMap.get(tWordsIndexArray.get(t)) + " " + phi[i][tWordsIndexArray.get(t)] + "\t");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            writer.write("\n");
        }
        writer.close();
    }

    public double[][] getTheta(int iteration, Documents docSet) {
        return theta;
    }

    public int getM() {
        return M;
    }

    public int getK() {
        return K;
    }

    public class TwordsComparable implements Comparator<Integer> {

        public double[] sortProb; // Store probability of each word in topic k

        public TwordsComparable(double[] sortProb) {
            this.sortProb = sortProb;
        }

        @Override
        public int compare(Integer o1, Integer o2) {
            // TODO Auto-generated method stub
            //Sort topic word index according to the probability of each word in topic k
            if (sortProb[o1] > sortProb[o2]) return -1;
            else if (sortProb[o1] < sortProb[o2]) return 1;
            else return 0;
        }
    }

    public static double getPerplexity(int N, int topicNum, String thetaFile, String phiFile) {
        double result;
        double[][] theta = getThetaArray(topicNum, thetaFile);
        Map<String, Map<Integer, Double>> twordsMap = getTwordsMap(phiFile);
        Set<String> twords = getWordList(phiFile);
        double wordCount = 0;
        if (N == 0) {
            countTotalWord(phiFile);
        } else {
            wordCount = N;
        }
        double sum = 0;
        for (String w : twords) {
            double wSum = 0;
            Map<Integer, Double> topicWeight = twordsMap.get(w);
            for (Integer topicIndex : topicWeight.keySet()) {
                for (int j = 0; j < theta.length; j++) {
                    wSum += topicWeight.get(topicIndex) * theta[j][topicIndex];
                }
            }
            double log = Math.log(wSum);
            sum += log;
        }
        result = Math.pow(Math.E, -(sum / wordCount));
        return result;
    }

    private static double[][] getThetaArray(int num, String thetaFile) {
        try {
            List<String> theta = Files.readLines(new File(thetaFile), Charsets.UTF_8);
            double[][] thetaArray = new double[theta.size()][num];
            for (int i = 0; i < thetaArray.length; i++) {
                for (int j = 0; j < thetaArray[i].length; j++) {
                    thetaArray[i][j] = 0;
                }
            }
            for (int i = 0; i < theta.size(); i++) {
                String item = theta.get(i);
                String[] tmp = item.split("\t");
                for (int j = 1; j < tmp.length; j++) {
                    thetaArray[i][j - 1] = Double.parseDouble(tmp[j]);
                }
            }
            return thetaArray;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return new double[0][num];
    }

    private static int countTotalWord(String twordsFile) {
        int result = 0;
        try {
            List<String> lines = Files.readLines(new File(twordsFile), Charsets.UTF_8);
            for (String line : lines) {
                String[] items = line.split("\t");
                result += items.length;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    private static Set<String> getWordList(String twordsFile) {
        Set<String> result = new HashSet<>();
        try {
            List<String> lines = Files.readLines(new File(twordsFile), Charsets.UTF_8);
            lines.forEach(line -> {
                String[] items = line.split("\t");
                for (String item : items) {
                    String[] wordWeight = item.split(";");
                    result.add(wordWeight[0]);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    private static Map<String, Map<Integer, Double>> getTwordsMap(String twordsFile) {
        Map<String, Map<Integer, Double>> result = new HashMap<>();
        try {
            List<String> lines = Files.readLines(new File(twordsFile), Charsets.UTF_8);
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                String[] items = line.split("\t");
                for (String item : items) {
                    String[] wordWeight = item.split(";");
                    String word = wordWeight[0];
                    Double weight = Double.valueOf(wordWeight[1]);
                    if (result.containsKey(word)) {
                        result.get(word).put(i, weight);
                    } else {
                        Map<Integer, Double> topicWordWeightMap = new HashMap<>();
                        topicWordWeightMap.put(i, weight);
                        result.put(word, topicWordWeightMap);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

}
