package com.stephen.lab.dto.analysis;

/**
 * Created by stephen on 2018/1/6.
 */
public class Token {
    private String word;
    private double weight;

    public Token() {

    }

    public Token(String s) {
        setWord(s);
    }

    public Token(Token token) {
        if (token != null) {
            setWord(token.getWord());
            setWeight(token.getWeight());
        }
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
        if (obj instanceof Token) {
            if (obj != null) {
                if (((Token) obj).getWord().equals(word)) {
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
