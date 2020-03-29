package com.cwj.mvn.framework.http.bean;

public class HttpRequest {

    private String protocol; // http or https
    private String method; // support GET and POST
    private String domain;
    private String route;
}