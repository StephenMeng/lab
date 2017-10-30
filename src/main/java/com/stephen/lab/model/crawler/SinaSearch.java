package com.stephen.lab.model.crawler;

import javax.persistence.Column;

/**
 * Created by stephen on 2017/10/22.
 */

public class SinaSearch extends ShiJiuDaMessage {
    @Column(name = "source_id")
    private Integer sourceId;
    @Column(name = "keyword")
    private String keyword;
    @Column(name = "content")
    private String content;

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
}
