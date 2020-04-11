package com.cwj.mvn.framework.http.bean;

import java.util.List;

import com.cwj.mvn.framework.Settings;
import com.cwj.mvn.utils.ByteUtils;

public class HttpRequest {
    
    private static final String LOCAL_URL_SUFFIX = Settings.getSetting(Settings.LOCAL_URL_SUFFIX); // 从配置文件中获取URL后缀
    
    private static final byte[] CHUNK_BREAK = {13, 10, 13, 10}; // CR LF CR LF
    private static final byte[] LINE_BREAK = {13, 10}; // CR LF
    private static final byte SPLIT_BREAK = 32; // " "
    private static final byte HEADER_BREAK = 58; // ":"
    private static final byte QUESTION_MASK = 63; // "?"
    private static final String PARAMETER_BREAK = "&"; // 38
    private static final String PARAMETER_VALUE_BREAK = "="; // 61
    
    private String protocol; // HTTP/1.0 or HTTP/1.1
    private String method; // support GET and POST
    private String route; 
    private HttpHeader headers;
    private HttpParameter parameters;

    public HttpRequest(byte[] buffer) {
        List<byte[]> requestArr = ByteUtils.split(buffer, CHUNK_BREAK);
        if (requestArr.size() == 0) {
            throw new RuntimeException("Http request string format error");
        }
        // 解析请求主体
        // -- 解析 method, route, protocol和URL参数
        List<byte[]> bodyArr = ByteUtils.split(requestArr.get(0), LINE_BREAK);
        List<byte[]> urlArr = ByteUtils.split(bodyArr.get(0), SPLIT_BREAK);
        if (urlArr.size() != 3) {
            throw new RuntimeException("URL format error");
        }
        headers = new HttpHeader();
        parameters = new HttpParameter();
        
        method = new String(urlArr.get(0));
        route = new String(urlArr.get(1));
        protocol = new String(urlArr.get(2));
        
        // ---- 路由要减去默认部分
        int suffixIndex = route.indexOf(LOCAL_URL_SUFFIX);
        if (suffixIndex == 0) {
            route = route.substring(LOCAL_URL_SUFFIX.length());
        }
        
        int questionMaskIndex = route.indexOf(QUESTION_MASK); // 解析跟在URL后面的参数
        if (questionMaskIndex != -1 && questionMaskIndex != route.length() - 1) {
            String[] parametersArr = route.substring(questionMaskIndex + 1).split(PARAMETER_BREAK);
            route = route.substring(0, questionMaskIndex);
            for (String parametersStr : parametersArr) {
                String[] parameter = parametersStr.split(PARAMETER_VALUE_BREAK);
                if (parameter.length == 2) {
                    parameters.put(parameter[0], parameter[1]);
                }
            }
        }
        // -- 解析Header
        for (int i = 1; i < bodyArr.size(); i++) {
            String headerStr = new String(bodyArr.get(i));
            int colonIndex = headerStr.indexOf(HEADER_BREAK);
            if (colonIndex == -1) throw new RuntimeException("Http header format error");
            headers.put(headerStr.substring(0, colonIndex).trim(), headerStr.substring(colonIndex + 1).trim());
        }
        // 解析请求数据
        int contentLength = Integer.parseInt(headers.getOrDefault(HttpHeader.CONTENT_LENGTH, "0"));
        if (contentLength > 0 && requestArr.size() > 1) {
            String contentType = headers.get(HttpHeader.CONTENT_TYPE);
            parameters.putParam(contentType, requestArr.get(1));
        }
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
