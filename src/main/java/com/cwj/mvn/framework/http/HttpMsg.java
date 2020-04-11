package com.cwj.mvn.framework.http;

public enum HttpMsg {
    OK(200);

    private int httpCode;
    HttpMsg(int httpCode) {
        this.httpCode = httpCode;
    }
    public int code() {
        return this.httpCode;
    }
    
    public String msg() {
        return this.toString();
    }
    
    public static HttpMsg getHttpMsg(int code, String msg) {
        for (HttpMsg httpMsg : values()) {
            if (httpMsg.code() == code && httpMsg.msg().equals(msg)) return httpMsg;
        }
        throw new NullPointerException("Cannot found code " + code + ", msg " + msg);
    }
}
