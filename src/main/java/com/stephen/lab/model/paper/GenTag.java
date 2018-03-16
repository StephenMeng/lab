package com.stephen.lab.model.paper;

import com.stephen.lab.dto.analysis.Token;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by stephen on 2018/3/15.
 */
public class GenTag {
    private List<Token> tfIdf;
    private List<Token> textRank;
    private List<Token> knn;
    private List<Token> classify;

    public GenTag() {
    }

    public List<Token> getTfIdf() {
        return tfIdf;
    }

    public void setTfIdf(List<Token> tfIdf) {
        this.tfIdf = tfIdf;
    }

    public List<Token> getTextRank() {
        return textRank;
    }

    public void setTextRank(List<Token> textRank) {
        this.textRank = textRank;
    }

    public List<Token> getKnn() {
        return knn;
    }

    public void setKnn(List<Token> knn) {
        this.knn = knn;
    }

    public List<Token> getClassify() {
        return classify;
    }

    public void setClassify(List<Token> classify) {
        this.classify = classify;
    }
}
