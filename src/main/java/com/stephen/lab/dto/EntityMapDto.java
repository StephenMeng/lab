package com.stephen.lab.dto;

public class EntityMapDto {
    private Integer mapId;
    private String entityA;
    private String relation;
    private String entityB;

    public Integer getMapId() {
        return mapId;
    }

    public void setMapId(Integer mapId) {
        this.mapId = mapId;
    }

    public String getEntityA() {
        return entityA;
    }

    public void setEntityA(String entityA) {
        this.entityA = entityA;
    }

    public String getRelation() {
        return relation;
    }

    public void setRelation(String relation) {
        this.relation = relation;
    }

    public String getEntityB() {
        return entityB;
    }

    public void setEntityB(String entityB) {
        this.entityB = entityB;
    }
}
