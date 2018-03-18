package com.stephen.lab.model.paper;

import com.stephen.lab.dto.analysis.FreqToken;
import com.stephen.lab.dto.analysis.Token;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by stephen on 2018/3/5.
 */
public class KivaResult {
    private Kiva kiva;
    private List<FreqToken> tokenList;
    private Double distanceToOther;

    public KivaResult() {
    }

    public KivaResult(Kiva k) {
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

    public List<FreqToken> getTokenList() {
        return tokenList;
    }

    public void setTokenList(List<FreqToken> tokenList) {
        this.tokenList = tokenList;
    }

    public void addToken(FreqToken token) {
        if (tokenList == null) {
            tokenList = new ArrayList<>();
        }
        tokenList.add(token);
    }

    public Double getDistanceToOther() {
        return distanceToOther;
    }

    public void setDistanceToOther(Double distanceToOther) {
        this.distanceToOther = distanceToOther;
    }

}
