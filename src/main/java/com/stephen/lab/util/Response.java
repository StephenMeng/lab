package com.stephen.lab.util;

/**
 * Created by stephen on 2017/7/15.
 */
public class Response {
    private Integer code;
    private String msg;
    private Object data;
    public static Response success(Object object) {
        Response response=new Response();
        response.setCode(200);
        response.setMsg("");
        response.setData(object);
        return response;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
