package com.stephen.lab.model.paper;

import com.stephen.lab.dto.analysis.Token;

import java.util.List;

/**
 * Created by stephen on 2018/3/15.
 */
public class GenTag {
    private List<Token> tfIdf;
    private List<Token> textRank;
    private List<Token> knn;
    private List<Token> svmKnn;
    private List<Token> svmLda;
    private List<Token> lda;
    private List<Token> filterKnn;
    private List<Token> filterLda;
    private List<Token> expandRank;
    private List<Token> mixture;

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

    public List<Token> getSvmKnn() {
        return svmKnn;
    }

    public void setSvmKnn(List<Token> svmKnn) {
        this.svmKnn = svmKnn;
    }

    public List<Token> getLda() {
        return lda;
    }

    public void setLda(List<Token> lda) {
        this.lda = lda;
    }

    public List<Token> getSvmLda() {
        return svmLda;
    }

    public void setSvmLda(List<Token> svmLda) {
        this.svmLda = svmLda;
    }

    public List<Token> getFilterKnn() {
        return filterKnn;
    }

    public void setFilterKnn(List<Token> filterKnn) {
        this.filterKnn = filterKnn;
    }

    public List<Token> getFilterLda() {
        return filterLda;
    }

    public void setFilterLda(List<Token> filterLda) {
        this.filterLda = filterLda;
    }

    public List<Token> getExpandRank() {
        return expandRank;
    }

    public void setExpandRank(List<Token> expandRank) {
        this.expandRank = expandRank;
    }

    public List<Token> getMixture() {
        return mixture;
    }

    public void setMixture(List<Token> mixture) {
        this.mixture = mixture;
    }
}
