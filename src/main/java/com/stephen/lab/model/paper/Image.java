package com.stephen.lab.model.paper;

import java.io.Serializable;

public class Image implements Serializable {
    private Long id;
    private Integer templateId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getTemplateId() {
        return templateId;
    }

    public void setTemplateId(Integer templateId) {
        this.templateId = templateId;
    }
}
