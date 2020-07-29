package com.cwj.mvn.framework.http.bean;

import java.net.HttpURLConnection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cwj.mvn.constant.Constant;
import com.cwj.mvn.utils.ByteUtils;
import com.cwj.mvn.utils.HttpUtils;

public class HttpRequest {
    
    private static final Logger log = LoggerFactory.getLogger(HttpRequest.class);
    
    private static final byte[] CHUNK_BREAK = {13, 10, 13, 10}; // CR LF CR LF
    private static final byte[] LINE_BREAK = {13, 10}; // CR LF
    private static final byte SPLIT_BREAK = 32; // " "
    private static final byte HEADER_BREAK = 58; // ":"
    private static final byte QUESTION_MASK = 63; // "?"
    private static final String PARAMETER_BREAK = "&"; // 38
    private static final String PARAMETER_VALUE_BREAK = "="; // 61
    
    private String protocol; // HTTP/1.0 or HTTP/1.1
    private String method; // support GET and POST
    private String path; // 具体文件路径 
    private String route; // 完整的URL
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
        log.info("Receive {} {} {}", method, route, protocol);
        
        // ---- 路径要减去默认部分
        int suffixIndex = route.indexOf(Constant.LOCAL_URL_SUFFIX);
        path = suffixIndex == 0 ? route.substring(Constant.LOCAL_URL_SUFFIX.length()) : route;
        
        int questionMaskIndex = path.indexOf(QUESTION_MASK); // 解析跟在URL后面的参数
        if (questionMaskIndex != -1 && questionMaskIndex != path.length() - 1) {
            String[] parametersArr = path.substring(questionMaskIndex + 1).split(PARAMETER_BREAK);
            path = path.substring(0, questionMaskIndex);
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
            parameters.putParam(HttpParameter.DATA, requestArr.get(1));
        }
    }
    
    /**
     * Request转byte
     * @return
     */
    public byte[] toArray(String route) {
        List<Byte> buffer = new LinkedList<>();
        addBytes(buffer, method.getBytes());
        buffer.add(SPLIT_BREAK);
        addBytes(buffer, route.getBytes());
        addBytes(buffer, path.getBytes());
        
        byte[] data = (byte[]) parameters.remove(HttpParameter.DATA);
        if (parameters.size() > 0) {
            buffer.add(QUESTION_MASK);
            int index = 0;
            for (Map.Entry<String, Object> param : parameters.entrySet()) {
                addBytes(buffer, param.getKey().getBytes());
                addBytes(buffer, PARAMETER_VALUE_BREAK.getBytes());
                addBytes(buffer, param.getValue().toString().getBytes());
                if (++index < parameters.size()) addBytes(buffer, PARAMETER_BREAK.getBytes());
            }
        }
        buffer.add(SPLIT_BREAK);
        addBytes(buffer, protocol.getBytes());
        buffer.add(SPLIT_BREAK);
        addBytes(buffer, LINE_BREAK);
        
        int index = 0;
        for (Map.Entry<String, String> header : headers.entrySet()) {
            addBytes(buffer, header.getKey().getBytes());
            buffer.add(HEADER_BREAK);
            addBytes(buffer, header.getValue().getBytes());
            if (++index < headers.size()) addBytes(buffer, LINE_BREAK);
        }
        addBytes(buffer, LINE_BREAK);
        if (data != null) addBytes(buffer, data);
        byte[] res = new byte[buffer.size()];
        int i = 0;
        for (Byte b : buffer) {
            res[i++] = b;
        }
        return res;
    }
    
    public HttpURLConnection toHttp(String remoteURL) {
        HttpURLConnection conn = null;
        try {
            if (!remoteURL.endsWith("/")) remoteURL += "/";
            String urlStr = remoteURL + path.substring(1);
            log.info("HTTP {}", urlStr);
            conn = HttpUtils.getConn(urlStr, method, headers, parameters);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return conn;
    }
    
    private void addBytes(List<Byte> buffer, byte... bs) {
        for (byte b : bs) buffer.add(b);
    }

    public String getProtocol() {
        return protocol;
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public String getRoute() {
        return route;
    }

    public HttpHeader getHeaders() {
        return headers;
    }

    public HttpParameter getParameters() {
        return parameters;
    }
}
