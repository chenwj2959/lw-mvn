package com.cwj.mvn.framework.http.bean;

import java.util.HashMap;

/**
 * TODO multipart/form-data
 */
public class HttpHeader extends HashMap<String, String> {

    private static final long serialVersionUID = 1L;
    // HTTP header name
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String CONTENT_LENGTH = "Content-Length";
    
    // Content-Type
    public static final String TYPE_URLENCODE = "application/x-www-form-urlencoded";
    public static final String TYPE_MULTIPART = "multipart/form-data";
    public static final String TYPE_TEXT_PLAIN = "text/plain";
    public static final String TYPE_JSON = "application/json";
}
