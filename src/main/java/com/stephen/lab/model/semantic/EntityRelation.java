package com.stephen.lab.model.semantic;

import javax.persistence.*;
import javax.persistence.Entity;

@Entity
@Table(name = "entity_relation")
public class EntityRelation {
    @Id
    @Column(name = "relation_id")
    private Long relationId;
    private String relationName;
    private String relationNamePassive;
    private Integer relationType;

    public Long getRelationId() {
        return relationId;
    }

    @Basic
    @Column(name = "relation_name")
    public String getRelationName() {
        return relationName;
    }

    @Basic
    @Column(name = "relation_name_passive")
    public String getRelationNamePassive() {
        return relationNamePassive;
    }

    @Basic
    @Column(name = "relation_type")
    public Integer getRelationType() {
        return relationType;
    }

    public void setRelationNamePassive(String relationNamePassive) {
        this.relationNamePassive = relationNamePassive;
    }

    public void setRelationId(Long relationId) {
        this.relationId = relationId;
    }

    public void setRelationType(Integer relationType) {
        this.relationType = relationType;
    }

    public void setRelationName(String relationName) {
        this.relationName = relationName;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof EntityRelation) {
            if (((EntityRelation) obj).getRelationId().equals(this.getRelationId())) {
                return true;
            }
        }
        return super.equals(obj);
    }
}
