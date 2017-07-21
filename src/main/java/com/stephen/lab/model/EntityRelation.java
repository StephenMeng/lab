package com.stephen.lab.model;

import javax.persistence.*;
import javax.persistence.Entity;

@Entity
@Table(name = "entity_relation")
public class EntityRelation {
    @Id
    @Column(name = "relation_id")
    private Integer relationId;
    private String relationName;

    public Integer getRelationId() {
        return relationId;
    }
    @Basic
    @Column(name = "relation_name")
    public String getRelationName() {
        return relationName;
    }

    public void setRelationId(Integer relationId) {
        this.relationId = relationId;
    }

    public void setRelationName(String relationName) {
        this.relationName = relationName;
    }
}
