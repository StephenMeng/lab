package com.stephen.lab.constant.semantic;

public enum EntityRelationType {
    SUB_CLASS_OF(1, "subClassOf"),
    COMMENT(2, "comment"),
    EQUIVALENT_CLASS(3, "equivalentClass"),
    ON_PROPERTY(4, "onProperty"),
    SOME_VALUE_FROM(5, "someValuesFrom");

    private Integer code;
    private String name;

    EntityRelationType(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
