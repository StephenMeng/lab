package com.stephen.lab.model;

import javax.persistence.*;
import javax.persistence.Entity;

@Entity
@Table(name = "entity_map")
public class EntityMap {
    @Id
    @Column(name = "map_id")
    private Integer mapId;
    private Integer entityA;
    private Integer relation;
    private Integer entityB;
    private Integer entityBType;
    private String text;
    private String property;
    public Integer getMapId() {
        return mapId;
    }

    public void setMapId(Integer mapId) {
        this.mapId = mapId;
    }
    @Basic
    @Column(name = "entity_a")
    public Integer getEntityA() {
        return entityA;
    }

    public void setEntityA(Integer entityA) {
        this.entityA = entityA;
    }
    @Basic
    @Column(name = "relation")
    public Integer getRelation() {
        return relation;
    }

    public void setRelation(Integer relation) {
        this.relation = relation;
    }
    @Basic
    @Column(name = "entity_b")
    public Integer getEntityB() {
        return entityB;
    }
    @Basic
    @Column(name = "entity_b_type")
    public Integer getEntityBType() {
        return entityBType;
    }
    @Basic
    @Column(name = "property")
    public String getProperty() {
        return property;
    }
    @Basic
    @Column(name = "text")
    public String getText() {
        return text;
    }

    public void setEntityB(Integer entityB) {
        this.entityB = entityB;
    }

    public void setEntityBType(Integer entityBType) {
        this.entityBType = entityBType;
    }

    public void setProperty(String property) {
        this.property = property;
    }
}
