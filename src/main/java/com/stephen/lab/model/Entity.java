package com.stephen.lab.model;

import javax.persistence.*;

@javax.persistence.Entity
@Table(name = "entity")
public class Entity {
    @Id
    @Column(name = "entity_id")
    private Integer entityId;
    private String entityName;
    private Integer entityType;
    public Integer getEntityId() {
        return entityId;
    }
    @Basic
    @Column(name = "entity_name")
    public String getEntityName() {
        return entityName;
    }
    @Basic
    @Column(name = "entity_type")
    public Integer getEntityType() {
        return entityType;
    }

    public void setEntityType(Integer entityType) {
        this.entityType = entityType;
    }

    public void setEntityId(Integer entityId) {
        this.entityId = entityId;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }
}
