package com.stephen.lab.model.condition;

import java.util.Date;
import java.util.List;

public class PaperSearchCondition {
    private Integer searchType;
    private List<Integer> sources;
    private String q;
    private String author;
    private Date startDate;
    private Date endDate;
    private String organ;
    private String journal;
    private String fund;
    private Integer pageNo;
    private Integer pageSize;

    public Integer getSearchType() {
        return searchType;
    }

    public List<Integer> getSources() {
        return sources;
    }

    public String getQ() {
        return q;
    }

    public void setQ(String q) {
        this.q = q;
    }

    public void setSearchType(Integer searchType) {
        this.searchType = searchType;
    }

    public void setSources(List<Integer> sources) {
        this.sources = sources;
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

    public Integer getPageNo() {
        return pageNo;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageNo(Integer pageNo) {
        this.pageNo = pageNo;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    @Override
    public String toString() {
        return "PaperSearchCondition{" +
                "searchType=" + searchType +
                ", sources=" + sources +
                ", q='" + q + '\'' +
                ", author='" + author + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", organ='" + organ + '\'' +
                ", journal='" + journal + '\'' +
                ", fund='" + fund + '\'' +
                '}';
    }
}
