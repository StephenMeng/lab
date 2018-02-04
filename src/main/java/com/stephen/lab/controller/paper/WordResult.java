package com.stephen.lab.controller.paper;

public class WordResult {
    private String word;
    private double lost;
    private int breakPoint;
    private int before;
    private int after;
    private double score;
    private int freq;

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

    public int getBefore() {
        return before;
    }

    public void setBefore(int before) {
        this.before = before;
    }

    public int getAfter() {
        return after;
    }

    public void setAfter(int after) {
        this.after = after;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
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
        return "WordResult{" +
                "lost=" + lost +
                ", breakPoint=" + breakPoint +
                ", before=" + before +
                ", after=" + after +
                ", score=" + score +
                '}';
    }
}
