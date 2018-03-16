package com.stephen.lab.dto.analysis;

/**
 * Created by stephen on 2018/1/6.
 */
public class BaseToken {
    private String word;
    private double weight;

    public BaseToken() {

    }

    public BaseToken(String s) {
        setWord(s);
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BaseToken) {
            if (obj != null) {
                if (((BaseToken) obj).getWord().equals(word)) {
                    return true;
                }
            }
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return word.hashCode();
    }
}
