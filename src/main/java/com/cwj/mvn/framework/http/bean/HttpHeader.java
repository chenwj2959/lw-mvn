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
    public static final String DATE = "Date";
    public static final String SERVER = "Server";
    public static final String X_CONTENT_TYPE_OPTIONS = "X-Content-Type-Options";
    public static final String CONTENT_SECURITY_POLICY = "Content-Security-Policy";
    public static final String LAST_MODIFIED = "Last-Modified";
    public static final String ETAG = "ETag";
    
    // Content-Type
    public static final String TYPE_URLENCODE = "application/x-www-form-urlencoded";
    public static final String TYPE_MULTIPART = "multipart/form-data";
    public static final String TYPE_TEXT_PLAIN = "text/plain";
    public static final String TYPE_JSON = "application/json";
    public static final String TYPE_XML = "application/xml";
    public static final String TYPE_JAVA_ARCHIVE = "application/java-archive"; // download jar
}
