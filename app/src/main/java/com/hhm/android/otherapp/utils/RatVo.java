package com.hhm.android.otherapp.utils;

import com.google.gson.JsonObject;

public class RatVo {

    private JsonObject common;
    private JsonObject params;

    private int code;
    private String msg;
    private long timestamp;

    private JsonObject data;

    public JsonObject getCommon() {
        return common;
    }

    public void setCommon(JsonObject common) {
        this.common = common;
    }

    public JsonObject getParams() {
        return params;
    }

    public void setParams(JsonObject params) {
        this.params = params;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public JsonObject getData() {
        return data;
    }

    public void setData(JsonObject data) {
        this.data = data;
    }

}
