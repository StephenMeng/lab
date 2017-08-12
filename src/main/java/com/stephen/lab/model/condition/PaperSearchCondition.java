package com.stephen.lab.model.condition;

import java.util.Date;

public class PaperSearchCondition {
    private String paperTitle;
    private String keyword;
    private String summary;
    private String author;
    private Date startDate;
    private Date endDate;
    private String organ;
    private String journal;
    private String fund;

    public String getPaperTitle() {
        return paperTitle;
    }

    public void setPaperTitle(String paperTitle) {
        this.paperTitle = paperTitle;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getOrgan() {
        return organ;
    }

    public void setOrgan(String organ) {
        this.organ = organ;
    }

    public String getJournal() {
        return journal;
    }

    public void setJournal(String journal) {
        this.journal = journal;
    }

    public String getFund() {
        return fund;
    }

    public void setFund(String fund) {
        this.fund = fund;
    }

    @Override
    public String toString() {
        return "PaperSearchCondition{" +
                "paperTitle='" + paperTitle + '\'' +
                ", keyword='" + keyword + '\'' +
                ", summary='" + summary + '\'' +
                ", author='" + author + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", organ='" + organ + '\'' +
                ", journal='" + journal + '\'' +
                ", fund='" + fund + '\'' +
                '}';
    }
}
