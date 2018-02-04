package com.stephen.lab.controller.paper;

import java.util.List;

public class LostPoint {
    private Integer start;
    private Integer end;
    private Integer num;
    private List<Integer>points;
    private double lost;
    public Integer getStart() {
        return start;
    }

    public void setStart(Integer start) {
        this.start = start;
    }

    public Integer getEnd() {
        return end;
    }

    public void setEnd(Integer end) {
        this.end = end;
    }

    public Integer getNum() {
        return num;
    }

    public void setNum(Integer num) {
        this.num = num;
    }

    public List<Integer> getPoints() {
        return points;
    }

    public void setPoints(List<Integer> points) {
        this.points = points;
    }

    public double getLost() {
        return lost;
    }

    public void setLost(double lost) {
        this.lost = lost;
    }
}
