package com.cwj.mvn.framework.http.bean;

import java.io.ByteArrayOutputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cwj.mvn.framework.http.HttpMsg;
import com.cwj.mvn.framework.socket.AbstractClientSocket;
import com.cwj.mvn.utils.ByteUtils;
import com.cwj.mvn.utils.DateUtils;

public class HttpResponse {
    
    private static final Logger log = LoggerFactory.getLogger(HttpResponse.class);
    
    private static final byte[] CHUNK_BREAK = {13, 10, 13, 10}; // CR LF CR LF
    private static final byte[] LINE_BREAK = {13, 10}; // CR LF
    private static final byte SPLIT_BREAK = 32; // " "
    private static final byte HEADER_BREAK = 58; // ":"
    
    private String protocol; // HTTP/1.0 or HTTP/1.1
    private HttpMsg httpMsg;
    private HttpHeader headers;
    private HttpParameter parameters;
    
    public HttpResponse(String protocol, int code, String msg) {
        this(protocol, HttpMsg.getHttpMsg(code, msg));
    }
    
    public HttpResponse(String protocol, HttpMsg httpMsg) {
        this.protocol = protocol;
        this.httpMsg = httpMsg;
        this.headers = new HttpHeader();
    }
    
    public static HttpResponse parse(byte[] buffer) {
        List<byte[]> respArr = ByteUtils.split(buffer, CHUNK_BREAK);
        if (respArr.size() == 0) {
            throw new RuntimeException("Http response string format error");
        }
        // 解析response主体
        List<byte[]> bodyArr = ByteUtils.split(respArr.get(0), LINE_BREAK);
        List<byte[]> urlArr = ByteUtils.split(bodyArr.get(0), SPLIT_BREAK);
        if (urlArr.size() != 3) {
            throw new RuntimeException("URL format error");
        }
        
        String protocol = new String(urlArr.get(0));
        Integer httpCode = Integer.parseInt(new String(urlArr.get(1)));
        String httpMsg = new String(urlArr.get(2));
        HttpResponse resp = new HttpResponse(protocol, httpCode, httpMsg);
        // -- 解析Header
        HttpHeader headers = resp.getHeaders();
        for (int i = 1; i < bodyArr.size(); i++) {
            String headerStr = new String(bodyArr.get(i));
            int colonIndex = headerStr.indexOf(HEADER_BREAK);
            if (colonIndex == -1) throw new RuntimeException("Http header format error");
            headers.put(headerStr.substring(0, colonIndex).trim(), headerStr.substring(colonIndex + 1).trim());
        }
        // 解析response数据
        int contentLength = Integer.parseInt(headers.getOrDefault(HttpHeader.CONTENT_LENGTH, "0"));
        if (contentLength > 0 && respArr.size() > 1) {
            HttpParameter httpParameter = new HttpParameter();
            httpParameter.put(HttpParameter.DATA, respArr.get(1));
            resp.setParameters(httpParameter);
        }
        return resp;
    }
    
    /**
     * 发送http response
     */
    public void send(AbstractClientSocket<byte[]> client) {
        headers.put(HttpHeader.DATE, DateUtils.dateToString(new Date(), DateUtils.EdMyHms_GMT));
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            // 写入缓冲
            byte[] buffer = null;
            if (parameters != null && (parameters.size() > 0 || parameters.get(HttpParameter.DATA) != null)) {
                String contentType = headers.getOrDefault(HttpHeader.CONTENT_TYPE, HttpHeader.TYPE_JSON);
                buffer = parameters.getData(contentType);
                headers.put(HttpHeader.CONTENT_LENGTH, Integer.toString(buffer.length));
            }
            // first line
            bos.write(protocol.getBytes());
            bos.write(SPLIT_BREAK);
            bos.write(Integer.toString(httpMsg.code()).getBytes());
            bos.write(SPLIT_BREAK);
            bos.write(httpMsg.msg().getBytes());
            bos.write(LINE_BREAK);
            log.info("Send {} {} {}", protocol, httpMsg.code(), httpMsg.msg());
            // headers
            for (Map.Entry<String, String> header : headers.entrySet()) {
                bos.write(header.getKey().getBytes());
                bos.write(HEADER_BREAK);
                bos.write(SPLIT_BREAK);
                bos.write(header.getValue().getBytes());
                bos.write(LINE_BREAK);
            }
            bos.write(LINE_BREAK);
            if (buffer != null) bos.write(buffer);
            client.send(bos.toByteArray());
        } catch (Exception e) {
            log.error("send response failed", e);
        }
    }
    
    public String getProtocol() {
        return protocol;
    }
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }
    public HttpMsg getHttpMsg() {
        return httpMsg;
    }
    public void setHttpMsg(HttpMsg httpMsg) {
        this.httpMsg = httpMsg;
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
