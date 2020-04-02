package com.cwj.mvn.framework.http.bean;

import com.alibaba.fastjson.JSON;
import com.cwj.mvn.framework.Settings;

public class HttpRequest {
    
    private static final String CHUNK_BREAK = "\\r\\n\\r\\n";
    private static final String LINE_BREAK = "\\r\\n";
    private static final String SPLIT_BREAK = " ";
    private static final String QUESTION_MASK = "?";
    private static final String PARAMETER_BREAK = "&";
    private static final String PARAMETER_VALUE_BREAK = "=";
    private static final String HEADER_BREAK = ":";
    
    private static final String LOCAL_URL_SUFFIX = Settings.getSetting(Settings.URL_SUFFIX); // 从配置文件中获取URL后缀

    private String protocol; // HTTP/1.0 or HTTP/1.1
    private String method; // support GET and POST
    private String route;
    private HttpHeader headers;
    private HttpParameter parameters;
    
    public HttpRequest(byte[] buffer) {
        String requestStr = new String(buffer);
        String[] requestArr = requestStr.split(CHUNK_BREAK);
        if (requestArr.length == 0) {
            throw new RuntimeException("Http request string format error");
        }
        // 解析请求主体
        // -- 解析 method, route, protocol和URL参数
        String[] bodyArr = requestArr[0].split(LINE_BREAK);
        String[] urlArr = bodyArr[0].split(SPLIT_BREAK);
        if (urlArr.length != 3) {
            throw new RuntimeException("URL format error");
        }
        headers = new HttpHeader();
        parameters = new HttpParameter();
        
        method = urlArr[0];
        route = urlArr[1];
        protocol = urlArr[2];
        
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
        for (int i = 1; i < bodyArr.length; i++) {
            int colonIndex = bodyArr[i].indexOf(HEADER_BREAK);
            if (colonIndex == -1) throw new RuntimeException("Http header format error");
            headers.put(bodyArr[i].substring(0, colonIndex).trim(), bodyArr[i].substring(colonIndex + 1).trim());
        }
        // 解析请求数据
        int contentLength = Integer.parseInt(headers.getOrDefault(HttpHeader.CONTENT_LENGTH, "0"));
        if (contentLength > 0 && requestArr.length > 1) {
            String requestData = requestArr[1];
            String contentType = headers.get(HttpHeader.CONTENT_TYPE);
            if (HttpHeader.TYPE_MULTIPART.equals(contentType)) {
                // FIXME 处理Multipart
                parameters.put(HttpParameter.DATA, requestData);
            } else if (HttpHeader.TYPE_URLENCODE.equals(contentType)) {
                // XXX 确认urlencode处理方式
                parameters.putAll(JSON.parseObject("{" + requestData + "}"));
            } else if (HttpHeader.TYPE_TEXT_PLAIN.equals(contentType)) {
                parameters.put(HttpParameter.DATA, requestData);
            } else if (HttpHeader.TYPE_JSON.equals(contentType)) {
                parameters.putAll(JSON.parseObject(requestData));
            } else {
                // XXX 完善content-type
                parameters.put(HttpParameter.DATA, requestData);
            }
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