package com.stephen.lab.model.paper;

public class YearSortedClusterResult extends SortedClusterResult {
    private int before;
    private int after;
    private double score;


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

}
