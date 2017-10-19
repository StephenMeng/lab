package com.stephen.lab.model.semantic;

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
    @Column(name = "paper_url")
    private String paperUrl;
    private Integer paperType;
    private String title;
    private String titleEn;
    private String author;
    @Column(name = "author_url")
    private String authorUrl;
    private String organ;
    private Integer source;
    @Column(name = "source_paper_id")
    private String sourcePaperId;
    private String keyword;
    private String summary;
    private String journal;
    private String year;
    private String period;
    private String fund;
    @Column(name = "classify_cn1")
    private String classifyCn1;
    @Column(name = "classify_cn2")
    private String classifyCn2;
    @Column(name = "issn")
    private String issn;
    @Column(name = "pub_date")
    private String pubDate;
    @Column(name = "read_num")
    private Integer readNum;

    public String getIssn() {
        return issn;
    }

    public void setIssn(String issn) {
        this.issn = issn;
    }

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
    @Column(name = "title_en")
    public String getTitleEn() {
        return titleEn;
    }

    public String getClassifyCn1() {
        return classifyCn1;
    }

    public String getClassifyCn2() {
        return classifyCn2;
    }

    public void setClassifyCn1(String classifyCn1) {
        this.classifyCn1 = classifyCn1;
    }

    public void setClassifyCn2(String classifyCn2) {
        this.classifyCn2 = classifyCn2;
    }

    public void setTitleEn(String titleEn) {
        this.titleEn = titleEn;
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

    public String getSourcePaperId() {
        return sourcePaperId;
    }

    public void setSourcePaperId(String sourcePaperId) {
        this.sourcePaperId = sourcePaperId;
    }

    public String getPaperUrl() {
        return paperUrl;
    }

    public String getAuthorUrl() {
        return authorUrl;
    }

    public void setPaperUrl(String paperUrl) {
        this.paperUrl = paperUrl;
    }

    public void setAuthorUrl(String authorUrl) {
        this.authorUrl = authorUrl;
    }

    public Integer getReadNum() {
        return readNum;
    }

    public String getPubDate() {
        return pubDate;
    }

    public void setPubDate(String pubDate) {
        this.pubDate = pubDate;
    }

    public void setReadNum(Integer readNum) {
        this.readNum = readNum;
    }

    @Override
    public String toString() {
        return "Paper{" +
                "paperId=" + paperId +
                ", paperDescription='" + paperDescription + '\'' +
                ", paperLinkId='" + paperLinkId + '\'' +
                ", paperUrl='" + paperUrl + '\'' +
                ", paperType=" + paperType +
                ", title='" + title + '\'' +
                ", titleEn='" + titleEn + '\'' +
                ", author='" + author + '\'' +
                ", authorUrl='" + authorUrl + '\'' +
                ", organ='" + organ + '\'' +
                ", issn='" + issn + '\'' +
                ", source=" + source +
                ", sourcePaperId='" + sourcePaperId + '\'' +
                ", keyword='" + keyword + '\'' +
                ", summary='" + summary + '\'' +
                ", journal='" + journal + '\'' +
                ", year='" + year + '\'' +
                ", period='" + period + '\'' +
                ", fund='" + fund + '\'' +
                ", classifyCn1='" + classifyCn1 + '\'' +
                ", classifyCn2='" + classifyCn2 + '\'' +
                ", pubDate='" + pubDate + '\'' +
                ", readNum='" + readNum + '\'' +
                '}';
    }
}
