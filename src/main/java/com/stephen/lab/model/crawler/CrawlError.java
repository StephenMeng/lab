package com.stephen.lab.model.crawler;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * Created by stephen on 2017/10/22.
 */
@Entity
@Table(name = "crawl_error")
public class CrawlError {
    @Id
    @Column(name = "id")
    private Integer id;
    @Column(name = "source_id")
    private Integer sourceId;
    @Column(name = "error_href")
    private String errorHref;
    @Column(name = "ext")
    private String ext;
    @Column(name = "create_date")
    private Date createDate;
    @Column(name = "status")
    private Integer status;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getSourceId() {
        return sourceId;
    }

    public void setSourceId(Integer sourceId) {
        this.sourceId = sourceId;
    }

    public String getErrorHref() {
        return errorHref;
    }

    public void setErrorHref(String errorHref) {
        this.errorHref = errorHref;
    }

    public String getExt() {
        return ext;
    }

    public void setExt(String ext) {
        this.ext = ext;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
