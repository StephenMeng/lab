package com.stephen.lab.model.crawler;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "crawl_gopolicy")
public class Gopolicy {
    @Id
    @Column(name = "url")
    private String url;
    @Column(name = "browser_num")
    private String browserNum;
    @Column(name = "download_num")
    private String downloadNum;
    @Column(name = "title")
    private String title;
    @Column(name = "abstract")
    private String abs;
    @Column(name = "pub_org")
    private String pubOrg;
    @Column(name = "pub_date")
    private String pubDate;
    @Column(name = "classify")
    private String classify;
    @Column(name = "code")
    private String code;
    @Column(name = "level")
    private String level;
    @Column(name = "keyword")
    private String keyword;
    @Column(name = "timeliness")
    private String timeliness;
    @Column(name = "content")
    private String content;
    @Column(name = "content_html")
    private String contentHtml;
    @Column(name = "full_text_url")
    private String full_text_url;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getBrowserNum() {
        return browserNum;
    }

    public void setBrowserNum(String browserNum) {
        this.browserNum = browserNum;
    }

    public String getDownloadNum() {
        return downloadNum;
    }

    public void setDownloadNum(String downloadNum) {
        this.downloadNum = downloadNum;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAbs() {
        return abs;
    }

    public void setAbs(String abs) {
        this.abs = abs;
    }

    public String getPubOrg() {
        return pubOrg;
    }

    public void setPubOrg(String pubOrg) {
        this.pubOrg = pubOrg;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getTimeliness() {
        return timeliness;
    }

    public void setTimeliness(String timeliness) {
        this.timeliness = timeliness;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getPubDate() {
        return pubDate;
    }

    public void setPubDate(String pubDate) {
        this.pubDate = pubDate;
    }

    public String getClassify() {
        return classify;
    }

    public void setClassify(String classify) {
        this.classify = classify;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getFull_text_url() {
        return full_text_url;
    }

    public void setFull_text_url(String full_text_url) {
        this.full_text_url = full_text_url;
    }

    public String getContentHtml() {
        return contentHtml;
    }

    public void setContentHtml(String contentHtml) {
        this.contentHtml = contentHtml;
    }
}
