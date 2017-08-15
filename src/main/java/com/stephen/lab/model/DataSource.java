package com.stephen.lab.model;

import javax.persistence.*;
import javax.persistence.Entity;

@Entity
@Table(name = "paper_source")
public class DataSource {
    @Id
    @Column(name = "source_id")
    private Integer sourceId;
    private String sourceName;
    private Integer sourceType;
    private String sourceUrl;

    public Integer getSourceId() {
        return sourceId;
    }

    @Basic
    @Column(name = "source_type")
    public Integer getSourceType() {
        return sourceType;
    }

    @Basic
    @Column(name = "source_name")
    public String getSourceName() {
        return sourceName;
    }

    @Basic
    @Column(name = "source_url")
    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceId(Integer sourceId) {
        this.sourceId = sourceId;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public void setSourceType(Integer sourceType) {
        this.sourceType = sourceType;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }
}
