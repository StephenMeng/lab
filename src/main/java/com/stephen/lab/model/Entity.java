package com.stephen.lab.model;

import javax.persistence.*;

@javax.persistence.Entity
@Table(name = "entity")
public class Entity {
    @Id
    @Column(name = "entity_id")
    private Integer entityId;
    private String entityName;

    public Integer getEntityId() {
        return entityId;
    }
    @Basic
    @Column(name = "entity_name")
    public String getEntityName() {
        return entityName;
    }

    public void setEntityId(Integer entityId) {
        this.entityId = entityId;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }
}
