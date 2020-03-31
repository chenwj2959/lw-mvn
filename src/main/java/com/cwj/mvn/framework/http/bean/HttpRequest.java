package com.cwj.mvn.framework.http.bean;

public class HttpRequest {

    private String protocol; // http or https
    private String method; // support GET and POST
    private String domain;
    private String route;
    private HttpHeader headers;
    private HttpParameter parameters;
    
    public HttpRequest(byte[] buffer) {
        
    }
    
    public String getProtocol() {
        return protocol;
    }
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }
    public String getMethod() {
        return method;
    }
    public void setMethod(String method) {
        this.method = method;
    }
    public String getDomain() {
        return domain;
    }
    public void setDomain(String domain) {
        this.domain = domain;
    }
    public String getRoute() {
        return route;
    }
    public void setRoute(String route) {
        this.route = route;
    }
    public HttpHeader getHeaders() {
        return headers;
    }
    public void setHeaders(HttpHeader headers) {
        this.headers = headers;
    }
    public HttpParameter getParameters() {
        return parameters;
    }
    public void setParameters(HttpParameter parameters) {
        this.parameters = parameters;
    }
}