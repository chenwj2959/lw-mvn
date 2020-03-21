package com.cwj.mvn.framework.http;

public class HttpRequest {

    private String method;
    
    private String path;
    
    private String protocol;
    
    public HttpRequest(String requestStr) {
        
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public String getProtocol() {
        return protocol;
    }
}