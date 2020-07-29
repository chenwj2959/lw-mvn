package com.cwj.mvn.framework.http;

public enum HttpMsg {
    OK(200, "OK"),
    MOVE_PERMANENTLY(301, "Moved Permanently"),
    NOT_FOUND(404, "Not Found");

    private int httpCode;
    private String httpMsg;
    HttpMsg(int httpCode, String httpMsg) {
        this.httpCode = httpCode;
        this.httpMsg = httpMsg;
    }
    public int code() {
        return this.httpCode;
    }
    
    public String msg() {
        return this.httpMsg;
    }
    
    public static HttpMsg getHttpMsg(int code, String msg) {
        for (HttpMsg httpMsg : values()) {
            if (httpMsg.code() == code && httpMsg.msg().equals(msg)) return httpMsg;
        }
        throw new NullPointerException("Cannot found code " + code + ", msg " + msg);
    }
}
