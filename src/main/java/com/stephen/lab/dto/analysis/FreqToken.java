package com.stephen.lab.dto.analysis;

/**
 * Created by stephen on 2018/1/6.
 */
public class FreqToken extends Token {
    private int freq;
    private int docCount;

    public FreqToken() {
        super();
    }

    public FreqToken(String s) {
        super(s);
    }

    public int getFreq() {
        return freq;
    }

    public void setFreq(int freq) {
        this.freq = freq;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof FreqToken) {
            if (obj != null) {
                if (((Token) obj).getWord().equals(getWord())) {
                    return true;
                }
            }
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return getWord().hashCode();
    }

    public void add(FreqToken token) {
        if (token.getWord().equals(getWord())) {
            setFreq(getFreq() + token.getFreq());
        }
    }

    public int getDocCount() {
        return docCount;
    }

    public void setDocCount(int docCount) {
        this.docCount = docCount;
    }
}
