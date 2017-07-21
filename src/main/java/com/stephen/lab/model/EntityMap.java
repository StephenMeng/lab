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

    public void setEntityB(Integer entityB) {
        this.entityB = entityB;
    }
}
