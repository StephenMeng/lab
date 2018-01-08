package com.stephen.lab.controller.analysis;

public class LostPoint {
    private Integer index;
    private double score;
    private LostPoint lostPoint;
    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public LostPoint getLostPoint() {
        return lostPoint;
    }

    public void setLostPoint(LostPoint lostPoint) {
        this.lostPoint = lostPoint;
    }
}
