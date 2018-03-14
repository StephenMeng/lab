package com.stephen.lab.controller.paper;

import com.stephen.lab.dto.analysis.Token;
import com.stephen.lab.model.paper.Kiva;
import com.stephen.lab.model.paper.KivaSimple;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by stephen on 2018/3/5.
 */
public class KivaResult {
    private Kiva kiva;
    private List<Token> tokenList;
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

    public List<Token> getTokenList() {
        return tokenList;
    }

    public void setTokenList(List<Token> tokenList) {
        this.tokenList = tokenList;
    }

    public void addToken(Token token) {
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
