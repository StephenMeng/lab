package com.stephen.lab.model;

import javax.persistence.*;
import javax.persistence.Entity;

@Entity
@Table(name = "paper")
public class Paper {
    @Id
    @Column(name = "paper_id")
    private Long paperId;
    private String paperDescription;
    private String paperLinkId;
    private Integer paperType;
    private String title;
    private String author;
    private String organ;
    private Integer source;
    private String keyword;
    private String summary;
    private String journal;
    private String year;
    private String period;
    private String fund;

    public Long getPaperId() {
        return paperId;
    }

    public void setPaperId(Long paperId) {
        this.paperId = paperId;
    }
    @Basic
    @Column(name = "paper_description")
    public String getPaperDescription() {
        return paperDescription;
    }

    public void setPaperDescription(String pDescription) {
        this.paperDescription = pDescription;
    }
    @Basic
    @Column(name = "paper_link_id")
    public String getPaperLinkId() {
        return paperLinkId;
    }

    public void setPaperLinkId(String paperLingId) {
        this.paperLinkId = paperLingId;
    }
    @Basic
    @Column(name = "paper_type")
    public Integer getPaperType() {
        return paperType;
    }

    public void setPaperType(Integer paperType) {
        this.paperType = paperType;
    }
    @Basic
    @Column(name = "title")
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
    @Basic
    @Column(name = "author")
    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }
    @Basic
    @Column(name = "organ")
    public String getOrgan() {
        return organ;
    }

    public void setOrgan(String organ) {
        this.organ = organ;
    }
    @Basic
    @Column(name = "source")
    public Integer getSource() {
        return source;
    }

    public void setSource(Integer source) {
        this.source = source;
    }
    @Basic
    @Column(name = "keyword")
    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }
    @Basic
    @Column(name = "summary")
    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }
    @Basic
    @Column(name = "journal")
    public String getJournal() {
        return journal;
    }

    public void setJournal(String journal) {
        this.journal = journal;
    }
    @Basic
    @Column(name = "year")
    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }
    @Basic
    @Column(name = "period")
    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }
    @Basic
    @Column(name = "fund")
    public String getFund() {
        return fund;
    }

    public void setFund(String fund) {
        this.fund = fund;
    }

    @Override
    public String toString() {
        return "Paper{" +
                "paperId=" + paperId +
                ", paperDescription='" + paperDescription + '\'' +
                ", paperLinkId='" + paperLinkId + '\'' +
                ", paperType=" + paperType +
                ", title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", organ='" + organ + '\'' +
                ", source=" + source +
                ", keyword='" + keyword + '\'' +
                ", summary='" + summary + '\'' +
                ", journal='" + journal + '\'' +
                ", year='" + year + '\'' +
                ", period='" + period + '\'' +
                ", fund='" + fund + '\'' +
                '}';
    }
}
