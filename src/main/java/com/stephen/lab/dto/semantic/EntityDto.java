package com.stephen.lab.dto.semantic;

import com.stephen.lab.model.semantic.Entity;

public class EntityDto {
    private Long entityId;
    private String entityName;
    private String entityType;
    private String typeNameShow;
    private Boolean isEntity;

    public EntityDto() {
    }

    public EntityDto(Entity entity) {
        this.entityId = entity.getEntityId();
        this.entityName = entity.getEntityName();
        this.entityType = entity.getEntityType();
    }

    public Long getEntityId() {
        return entityId;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public String getTypeNameShow() {
        return typeNameShow;
    }

    public void setTypeNameShow(String typeNameShow) {
        this.typeNameShow = typeNameShow;
    }

    public Boolean getIsEntity() {
        return isEntity;
    }

    public void setIsEntity(Boolean isEntity) {
        this.isEntity = isEntity;
    }

    public static EntityDto modelToDto(Entity entityB) {
        EntityDto dto = new EntityDto();
        dto.setEntityType(entityB.getEntityType());
        dto.setEntityName(entityB.getEntityName());
        dto.setEntityId(entityB.getEntityId());
        dto.setIsEntity(false);
        return dto;
    }
}
