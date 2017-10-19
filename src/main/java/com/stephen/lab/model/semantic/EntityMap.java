package com.stephen.lab.model.semantic;

import javax.persistence.*;
import javax.persistence.Entity;

@Entity
@Table(name = "entity_map")
public class EntityMap {
    @Id
    @Column(name = "map_id")
    private Integer mapId;
    private Long entityA;
    private Long relation;
    private Long entityB;
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
    public Long getEntityA() {
        return entityA;
    }

    public void setEntityA(Long entityA) {
        this.entityA = entityA;
    }

    @Basic
    @Column(name = "relation")
    public Long getRelation() {
        return relation;
    }

    public void setRelation(Long relation) {
        this.relation = relation;
    }

    @Basic
    @Column(name = "entity_b")
    public Long getEntityB() {
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

    public void setText(String text) {
        this.text = text;
    }

    public void setEntityB(Long entityB) {
        this.entityB = entityB;
    }

    public void setEntityBType(Integer entityBType) {
        this.entityBType = entityBType;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    @Override
    public String toString() {
        return "EntityMap{" +
                "mapId=" + mapId +
                ", entityA=" + entityA +
                ", relation=" + relation +
                ", entityB=" + entityB +
                ", entityBType=" + entityBType +
                ", text='" + text + '\'' +
                ", property='" + property + '\'' +
                '}';
    }
}
