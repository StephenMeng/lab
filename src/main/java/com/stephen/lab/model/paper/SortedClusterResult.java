package com.stephen.lab.model.paper;

import java.util.List;

public class SortedClusterResult {
    private String word;
    private double lost;
    private int breakPoint;
    private List<Integer> breakPoints;
    private int freq;

    public SortedClusterResult() {
        setFreq(0);
        setBreakPoint(0);
    }

    public double getLost() {
        return lost;
    }

    public void setLost(double lost) {
        this.lost = lost;
    }

    public int getBreakPoint() {
        return breakPoint;
    }

    public void setBreakPoint(int breakPoint) {
        this.breakPoint = breakPoint;
    }


    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public int getFreq() {
        return freq;
    }

    public void setFreq(int freq) {
        this.freq = freq;
    }

    public List<Integer> getBreakPoints() {
        return breakPoints;
    }

    public void setBreakPoints(List<Integer> breakPoints) {
        this.breakPoints = breakPoints;
    }

    @Override
    public String toString() {
        return "SortedClusterResult{" +
                "word='" + word + '\'' +
                ", lost=" + lost +
                ", breakPoint=" + breakPoint +
                ", breakPoints=" + breakPoints +
                ", freq=" + freq +
                '}';
    }
}
