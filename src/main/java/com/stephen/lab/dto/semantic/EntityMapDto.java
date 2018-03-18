package com.stephen.lab.dto.semantic;

import java.util.List;

public class EntityMapDto {
    private EntityDto entity;
    private List<RelationWithEntity> relationWithEntities;

    public EntityDto getEntity() {
        return entity;
    }

    public List<RelationWithEntity> getRelationWithEntities() {
        return relationWithEntities;
    }

    public void setEntity(EntityDto entity) {
        this.entity = entity;
    }

    public void setRelationWithEntities(List<RelationWithEntity> relationWithEntities) {
        this.relationWithEntities = relationWithEntities;
    }
}
