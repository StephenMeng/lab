package com.stephen.lab.constant.semantic;

public enum PaperType {
    JOURNAL_PAPER(1, "期刊论文"),

    BLOG(101, "博客");


    private Integer typeCode;
    private String typeName;

    PaperType(Integer typeCode, String typeName) {

    }

    public Integer getTypeCode() {
        return typeCode;
    }

    public void setTypeCode(Integer typeCode) {
        this.typeCode = typeCode;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }
}
