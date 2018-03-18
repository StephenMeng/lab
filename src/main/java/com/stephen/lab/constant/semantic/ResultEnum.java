package com.stephen.lab.constant.semantic;

public enum ResultEnum {
    FAIL_PARAM_WRONG(300, "parameters are wrong", "参数错误");

    private Integer code;
    private String msgEn;
    private String msgCn;

    ResultEnum(Integer code, String msgEn, String msgCn) {
        this.code = code;
        this.msgCn = msgCn;
        this.msgEn = msgEn;
    }

    public Integer getCode() {
        return code;
    }

    public String getMsgCn() {
        return msgCn;
    }

    public String getMsgEn() {
        return msgEn;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public void setMsgCn(String msgCn) {
        this.msgCn = msgCn;
    }

    public void setMsgEn(String msgEn) {
        this.msgEn = msgEn;
    }
}
