package com.cwj.mvn.framework.http.bean;

public class HttpRequest {
    
    private static final String LINE_BREAK = "\\n\\r";
    private static final String SPLIT_BREAK = " ";
    private static final String QUESTION_MASK = "?";
    private static final String PARAMETER_BREAK = "&";
    private static final String PARAMETER_VALUE_BREAK = "=";

    private String protocol; // HTTP/1.0 or HTTP/1.1
    private String method; // support GET and POST
    private String route;
    private HttpHeader headers;
    private HttpParameter parameters;
    
    public HttpRequest(String requestStr) {
        String[] requestArr = requestStr.split(LINE_BREAK);
        if (requestArr.length == 0) {
            throw new RuntimeException("Http request string format error");
        }
        String[] urlArr = requestArr[0].split(SPLIT_BREAK);
        if (urlArr.length != 3) {
            throw new RuntimeException("URL format error");
        }
        headers = new HttpHeader();
        parameters = new HttpParameter();
        
        method = urlArr[0];
        route = urlArr[1]; // TODO 路由要减去默认部分, 要解析GET参数
        protocol = urlArr[2];
        
        int questionMaskIndex = route.indexOf(QUESTION_MASK); // 解析跟在URL后面的参数
        if (questionMaskIndex != -1 && questionMaskIndex != route.length() - 1) {
            route = route.substring(0, questionMaskIndex);
            String[] parametersArr = route.substring(questionMaskIndex + 1).split(PARAMETER_BREAK);
            for (String parametersStr : parametersArr) {
                
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