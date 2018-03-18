package com.stephen.lab.model.paper;

public class SortedClusterResult {
    private String word;
    private double lost;
    private int breakPoint;
    private int freq;
    public SortedClusterResult(){
        setFreq(0);
        setBreakPoint(-1);
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

    @Override
    public String toString() {
        return "SortedClusterResult{" +
                "lost=" + lost +
                ", breakPoint=" + breakPoint +
                '}';
    }
}
