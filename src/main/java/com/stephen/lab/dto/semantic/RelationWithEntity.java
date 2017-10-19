package com.stephen.lab.dto.semantic;

import java.util.List;

public class RelationWithEntity {
    private Long relationId;
    private String relationName;
    private Integer relationType;

    private List<EntityDto>entities;

    public List<EntityDto> getEntities() {
        return entities;
    }

    public Integer getRelationType() {
        return relationType;
    }

    public Long getRelationId() {
        return relationId;
    }

    public String getRelationName() {
        return relationName;
    }

    public void setEntities(List<EntityDto> entities) {
        this.entities = entities;
    }

    public void setRelationType(Integer relationType) {
        this.relationType = relationType;
    }

    public void setRelationId(Long relationId) {
        this.relationId = relationId;
    }

    public void setRelationName(String relationName) {
        this.relationName = relationName;
    }
}
