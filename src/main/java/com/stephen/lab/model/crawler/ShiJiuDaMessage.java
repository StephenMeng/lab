package com.stephen.lab.model.crawler;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Created by stephen on 2017/10/22.
 */
@Entity
@Table(name = "crawl_sjd")
public class ShiJiuDaMessage {
    @Column(name = "id")
    private Integer id;
    @Id
    @Column(name = "href")
    private String href;
    @Column(name = "title")
    private String title;
    @Column(name = "pub_date")
    private String pubDate;
    @Column(name = "source_org")
    private String sourceOrg;
    @Column(name = "message_type")
    private String messageType;
    @Column(name = "scope_of_area")
    private String scopeOfArea;
    @Column(name = "scope_of_industry")
    private String scopeOfIndustry;
    @Column(name = "scope_of_theme")
    private String scopeOfTheme;
    @Column(name = "source_id")
    private Integer sourceId;
    @Column(name = "keyword")
    private String keyword;
    @Column(name = "content")
    private String content;
    @Column(name = "comment_count")
    private String commentCount;
    @Column(name = "editor")
    private String editor;
    @Column(name = "classify")
    private String classify;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPubDate() {
        return pubDate;
    }

    public void setPubDate(String pubDate) {
        this.pubDate = pubDate;
    }

    public String getSourceOrg() {
        return sourceOrg;
    }

    public void setSourceOrg(String sourceOrg) {
        this.sourceOrg = sourceOrg;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getScopeOfArea() {
        return scopeOfArea;
    }

    public void setScopeOfArea(String scopeOfArea) {
        this.scopeOfArea = scopeOfArea;
    }

    public String getScopeOfIndustry() {
        return scopeOfIndustry;
    }

    public void setScopeOfIndustry(String scopeOfIndustry) {
        this.scopeOfIndustry = scopeOfIndustry;
    }

    public String getScopeOfTheme() {
        return scopeOfTheme;
    }

    public void setScopeOfTheme(String scopeOfTheme) {
        this.scopeOfTheme = scopeOfTheme;
    }

    public Integer getSourceId() {
        return sourceId;
    }

    public void setSourceId(Integer sourceId) {
        this.sourceId = sourceId;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCommentCount() {
        return commentCount;
    }

    public String getEditor() {
        return editor;
    }

    public void setCommentCount(String commentCount) {
        this.commentCount = commentCount;
    }

    public void setEditor(String editor) {
        this.editor = editor;
    }

    public String getClassify() {
        return classify;
    }

    public void setClassify(String classify) {
        this.classify = classify;
    }

    @Override
    public String toString() {
        return "ShiJiuDaMessage{" +
                "id=" + id +
                ", href='" + href + '\'' +
                ", title='" + title + '\'' +
                ", pubDate='" + pubDate + '\'' +
                ", sourceOrg='" + sourceOrg + '\'' +
                ", messageType='" + messageType + '\'' +
                ", scopeOfArea='" + scopeOfArea + '\'' +
                ", scopeOfIndustry='" + scopeOfIndustry + '\'' +
                ", scopeOfTheme='" + scopeOfTheme + '\'' +
                ", sourceId=" + sourceId +
                ", keyword='" + keyword + '\'' +
                ", content='" + content + '\'' +
                ", editor='" + editor + '\'' +
                ", commentCount='" + commentCount + '\'' +
                ", classify='" + classify + '\'' +

                '}';
    }
}
