package com.stephen.lab.dto.semantic;

public class EntityGenIdDto {
    private String genId;
    private Long relationId;
    private String relation;
    private String ext;

    public String getGenId() {
        return genId;
    }

    public void setGenId(String genId) {
        this.genId = genId;
    }

    public Long getRelationId() {
        return relationId;
    }

    public void setRelationId(Long relationId) {
        this.relationId = relationId;
    }

    public String getRelation() {
        return relation;
    }

    public void setRelation(String relation) {
        this.relation = relation;
    }

    public String getExt() {
        return ext;
    }

    public void setExt(String ext) {
        this.ext = ext;
    }

    @Override
    public String toString() {
        return "EntityGenIdDto{" +
                "genId='" + genId + '\'' +
                ", relationId=" + relationId +
                ", relation='" + relation + '\'' +
                ", ext='" + ext + '\'' +
                '}';
    }
}
