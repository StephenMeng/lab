package com.stephen.lab.model.semantic;

import javax.persistence.*;

@javax.persistence.Entity
@Table(name = "entity")
public class Entity {
    @Id
    @Column(name = "entity_id")
    private Long entityId;
    private String entityName;
    private String entityType;

    public Long getEntityId() {
        return entityId;
    }

    @Basic
    @Column(name = "entity_name")
    public String getEntityName() {
        return entityName;
    }

    @Basic
    @Column(name = "entity_type")
    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    @Override
    public String toString() {
        return "Entity{" +
                "entityId=" + entityId +
                ", entityName='" + entityName + '\'' +
                ", entityType='" + entityType + '\'' +
                '}';
    }
}
