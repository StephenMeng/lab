package com.stephen.lab.model.others;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Created by stephen on 2018/4/20.
 */
@Entity
@Table(name = "crawl_bilibili_danmu")
public class BDanmu {
    @Column(name = "aid")
    private Long aid;
    @Column(name = "content")
    private String content;

    public Long getAid() {
        return aid;
    }

    public String getContent() {
        return content;
    }

    public void setAid(Long aid) {
        this.aid = aid;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
