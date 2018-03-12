package com.stephen.lab.controller.paper;

import com.stephen.lab.model.paper.Kiva;
import com.stephen.lab.model.paper.KivaSimple;

import java.util.Map;

/**
 * Created by stephen on 2018/3/5.
 */
public class KivaResult {
    private Kiva kiva;
    private Map<String,Integer>wordMap;
    private Map<String,Double>wordTfIdf;
    public KivaResult(){}
    public KivaResult(Kiva k){
        setKiva(k);
    }

    public KivaResult(KivaSimple kivaSimple) {
        setKiva(new Kiva(kivaSimple));
    }

    public Kiva getKiva() {
        return kiva;
    }

    public void setKiva(Kiva kiva) {
        this.kiva = kiva;
    }

    public void setWordMap(Map<String, Integer> wordMap) {
        this.wordMap = wordMap;
    }

    public Map<String, Integer> getWordMap() {
        return wordMap;
    }

    public Map<String, Double> getWordTfIdf() {
        return wordTfIdf;
    }

    public void setWordTfIdf(Map<String, Double> wordTfIdf) {
        this.wordTfIdf = wordTfIdf;
    }
}
