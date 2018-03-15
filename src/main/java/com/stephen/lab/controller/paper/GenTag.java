package com.stephen.lab.controller.paper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by stephen on 2018/3/15.
 */
public class GenTag {
    private List<String> tfIdf;
    private List<String> textRank;
    private List<String> knn;
    private List<String> classify;

    public GenTag() {
    }

    public List<String> getTfIdf() {
        return tfIdf;
    }

    public void setTfIdf(List<String> tfIdf) {
        this.tfIdf = tfIdf;
    }

    public List<String> getTextRank() {
        return textRank;
    }

    public void setTextRank(List<String> textRank) {
        this.textRank = textRank;
    }

    public List<String> getKnn() {
        return knn;
    }

    public void setKnn(List<String> knn) {
        this.knn = knn;
    }

    public List<String> getClassify() {
        return classify;
    }

    public void setClassify(List<String> classify) {
        this.classify = classify;
    }
}
