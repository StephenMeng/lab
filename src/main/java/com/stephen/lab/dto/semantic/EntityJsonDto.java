package com.stephen.lab.dto.semantic;

import java.util.List;

public class EntityJsonDto {
    private String id;
    private List<String> type;
    private List<String>subClassOf;
    private List<String>equivalentClass;
    private List<String>onProperty;
    private List<String>someValuesFrom;
    private List<String> comment;
    private List<String>annotatedProperty;
    private List<String>annotatedSource;
    private List<String>annotatedTarget;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<String> getType() {
        return type;
    }

    public void setType(List<String> type) {
        this.type = type;
    }

    public List<String> getSubClassOf() {
        return subClassOf;
    }

    public void setSubClassOf(List<String> subClassOf) {
        this.subClassOf = subClassOf;
    }

    public List<String> getEquivalentClass() {
        return equivalentClass;
    }

    public void setEquivalentClass(List<String> equivalentClass) {
        this.equivalentClass = equivalentClass;
    }

    public List<String> getOnProperty() {
        return onProperty;
    }

    public void setOnProperty(List<String> onProperty) {
        this.onProperty = onProperty;
    }

    public List<String> getSomeValuesFrom() {
        return someValuesFrom;
    }

    public void setSomeValuesFrom(List<String> someValuesFrom) {
        this.someValuesFrom = someValuesFrom;
    }

    public List<String> getComment() {
        return comment;
    }

    public void setComment(List<String> comment) {
        this.comment = comment;
    }

    public List<String> getAnnotatedProperty() {
        return annotatedProperty;
    }

    public void setAnnotatedProperty(List<String> annotatedProperty) {
        this.annotatedProperty = annotatedProperty;
    }

    public List<String> getAnnotatedSource() {
        return annotatedSource;
    }

    public void setAnnotatedSource(List<String> annotatedSource) {
        this.annotatedSource = annotatedSource;
    }

    public List<String> getAnnotatedTarget() {
        return annotatedTarget;
    }

    public void setAnnotatedTarget(List<String> annotatedTarget) {
        this.annotatedTarget = annotatedTarget;
    }

    @Override
    public String toString() {
        return "EntityJsonDto{" +
                "id='" + id + '\'' +
                ", type=" + type +
                ", subClassOf=" + subClassOf +
                ", equivalentClass=" + equivalentClass +
                ", onProperty=" + onProperty +
                ", someValuesFrom=" + someValuesFrom +
                ", comment=" + comment +
                ", annotatedProperty=" + annotatedProperty +
                ", annotatedSource=" + annotatedSource +
                ", annotatedTarget=" + annotatedTarget +
                '}';
    }
}
