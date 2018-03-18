package com.stephen.lab.dto.semantic;

import com.stephen.lab.constant.semantic.SourceType;
import com.stephen.lab.model.semantic.Paper;

public class PaperDto {
    private Long paperId;
    private String paperLinkId;
    private String paperUrl;
    private Integer paperType;
    private String paperTypeName;
    private String title;
    private String titleEn;
    private String author;
    private String authorUrl;
    private String organ;
    private Integer source;
    private String sourceName;
    private String sourceUrl;
    private String sourcePaperId;
    private String keyword;
    private String summary;
    private String journal;
    private String year;
    private String period;
    private String fund;
    private String classifyCn1;
    private String classifyCn2;
    private String issn;
    private String pubDate;
    private String readNum;

    public Long getPaperId() {
        return paperId;
    }

    public void setPaperId(Long paperId) {
        this.paperId = paperId;
    }

    public String getPaperLinkId() {
        return paperLinkId;
    }

    public void setPaperLinkId(String paperLinkId) {
        this.paperLinkId = paperLinkId;
    }

    public Integer getPaperType() {
        return paperType;
    }

    public void setPaperType(Integer paperType) {
        this.paperType = paperType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitleEn() {
        return titleEn;
    }

    public void setTitleEn(String titleEn) {
        this.titleEn = titleEn;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getOrgan() {
        return organ;
    }

    public void setOrgan(String organ) {
        this.organ = organ;
    }

    public Integer getSource() {
        return source;
    }

    public void setSource(Integer source) {
        this.source = source;
    }

    public String getSourcePaperId() {
        return sourcePaperId;
    }

    public void setSourcePaperId(String sourcePaperId) {
        this.sourcePaperId = sourcePaperId;
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

    public String getJournal() {
        return journal;
    }

    public void setJournal(String journal) {
        this.journal = journal;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public String getFund() {
        return fund;
    }

    public void setFund(String fund) {
        this.fund = fund;
    }

    public String getClassifyCn1() {
        return classifyCn1;
    }

    public void setClassifyCn1(String classifyCn1) {
        this.classifyCn1 = classifyCn1;
    }

    public String getClassifyCn2() {
        return classifyCn2;
    }

    public void setClassifyCn2(String classifyCn2) {
        this.classifyCn2 = classifyCn2;
    }

    public String getIssn() {
        return issn;
    }

    public void setIssn(String issn) {
        this.issn = issn;
    }

    public String getPaperTypeName() {
        return paperTypeName;
    }

    public void setPaperTypeName(String paperTypeName) {
        this.paperTypeName = paperTypeName;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public String getPaperUrl() {
        return paperUrl;
    }

    public String getPubDate() {
        return pubDate;
    }

    public void setPaperUrl(String paperUrl) {
        this.paperUrl = paperUrl;
    }

    public void setReadNum(String readNum) {
        this.readNum = readNum;
    }

    public void setPubDate(String pubDate) {
        this.pubDate = pubDate;
    }

    public void setAuthorUrl(String authorUrl) {
        this.authorUrl = authorUrl;
    }

    public String getAuthorUrl() {
        return authorUrl;
    }

    public String getReadNum() {
        return readNum;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public static PaperDto toDto(Paper paper) {
        PaperDto dto = new PaperDto();
        dto.setAuthor(paper.getAuthor());
        dto.setClassifyCn1(paper.getClassifyCn1());
        dto.setClassifyCn2(paper.getClassifyCn2());
        dto.setFund(paper.getFund());
        dto.setIssn(paper.getIssn());
        dto.setJournal(paper.getJournal());
        dto.setKeyword(paper.getKeyword() != null ? paper.getKeyword().substring(0, paper.getKeyword().length() - 1) : "");
        dto.setOrgan(paper.getOrgan() != null ? paper.getOrgan().substring(0, paper.getOrgan().length() - 1).replaceAll("；", ";") : "");
        dto.setYear(paper.getYear());
        dto.setTitleEn(paper.getTitleEn());
        dto.setTitle(paper.getTitle());
        dto.setPaperId(paper.getPaperId());
        dto.setPaperLinkId(paper.getPaperLinkId());
        dto.setPeriod(paper.getPeriod());
        dto.setSource(paper.getSource());
        dto.setSourcePaperId(paper.getSourcePaperId());
        dto.setSummary(paper.getSummary());
        dto.setPaperType(paper.getPaperType());
        dto.setPaperTypeName(paper.getPaperType().equals(1) ? "期刊论文" : "其他");
        dto.setSourceName(paper.getSource() == SourceType.CNKI ? "CNKI" :
                paper.getSource() == SourceType.CSSCI ? "CSSCI" : "博客论坛");
        dto.setPaperUrl(paper.getPaperUrl());
        dto.setAuthorUrl(paper.getAuthorUrl());
        dto.setReadNum(String.valueOf(paper.getReadNum()));
        dto.setPubDate(paper.getPubDate());
        return dto;
    }
}
