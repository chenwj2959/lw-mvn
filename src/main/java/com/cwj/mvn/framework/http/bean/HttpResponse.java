package com.cwj.mvn.framework.http.bean;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.util.Date;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cwj.mvn.framework.socket.AbstractClientSocket;
import com.cwj.mvn.utils.DateUtils;
import com.cwj.mvn.utils.SHAUtils;

public class HttpResponse {
    
    private static final Logger log = LoggerFactory.getLogger(HttpResponse.class);
    
    private static final String CHUNK_BREAK = "\\r\\n\\r\\n";
    private static final String LINE_BREAK = "\\r\\n";
    private static final String SPLIT_BREAK = " ";
    private static final String HEADER_BREAK = ":";
    private static final String SHA_FILE_SUFFIX = ".sha1";
    
    private String protocol; // HTTP/1.0 or HTTP/1.1
    private Integer httpCode; // 200
    private String httpMsg; // OK
    private HttpHeader headers;
    private File respFile;
    
    public HttpResponse(String protocol) {
        this.protocol = protocol;
        this.httpCode = 200;
        this.httpMsg = "OK";
        headers = new HttpHeader();
        headers.put(HttpHeader.SERVER, "LW-MAVEN/1.0.0");
        headers.put(HttpHeader.X_CONTENT_TYPE_OPTIONS, "nosniff");
        headers.put(HttpHeader.CONTENT_SECURITY_POLICY, "sandbox allow-forms allow-modals allow-popups allow-presentation allow-scripts allow-top-navigation");
    }
    
    public static HttpResponse parse(byte[] buffer) {
        String respStr = new String(buffer);
        String[] respArr = respStr.split(CHUNK_BREAK);
        if (respArr.length == 0) {
            throw new RuntimeException("Http response string format error");
        }
        // 解析response主体
        String[] bodyArr = respArr[0].split(LINE_BREAK);
        String[] urlArr = bodyArr[0].split(SPLIT_BREAK);
        if (urlArr.length != 3) {
            throw new RuntimeException("URL format error");
        }
        HttpResponse resp = new HttpResponse(urlArr[0]);
        resp.setHttpCode(Integer.parseInt(urlArr[1]));
        resp.setHttpMsg(urlArr[2]);
        // -- 解析Header
        HttpHeader headers = resp.getHeaders();
        headers.clear();
        for (int i = 1; i < bodyArr.length; i++) {
            int colonIndex = bodyArr[i].indexOf(HEADER_BREAK);
            if (colonIndex == -1) throw new RuntimeException("Http header format error");
            headers.put(bodyArr[i].substring(0, colonIndex).trim(), bodyArr[i].substring(colonIndex + 1).trim());
        }
        // 解析response数据
        int contentLength = Integer.parseInt(headers.getOrDefault(HttpHeader.CONTENT_LENGTH, "0"));
        if (contentLength > 0 && respArr.length > 1) {
        }
        return resp;
    }
    
    /**
     * 发送http response
     */
    public void send(AbstractClientSocket<byte[]> client) {
        headers.put(HttpHeader.DATE, DateUtils.dateToString(new Date(), DateUtils.EdMyHms_GMT));
        StringBuilder resp = new StringBuilder();
        // first line
        resp.append(protocol).append(SPLIT_BREAK).append(httpCode).append(SPLIT_BREAK).append(httpMsg).append(LINE_BREAK);
        // headers
        for (Map.Entry<String, String> header : headers.entrySet()) {
            resp.append(header.getKey()).append(HEADER_BREAK).append(SPLIT_BREAK).append(header.getValue()).append(LINE_BREAK);
        }
        resp.append(LINE_BREAK);
        // 写入缓冲
        if (respFile != null && respFile.exists()) {
            headers.put(HttpHeader.ETAG, readSHA1Str());
            headers.put(HttpHeader.LAST_MODIFIED, DateUtils.dateToString(respFile.lastModified(), DateUtils.EdMyHms_GMT));
            try (FileInputStream fis = new FileInputStream(respFile)) {
                int fileLen = fis.available();
                headers.put(HttpHeader.CONTENT_LENGTH, Integer.toString(fileLen));
                ByteBuffer buffer = ByteBuffer.allocateDirect(resp.length() + fileLen);
                buffer.put(resp.toString().getBytes());
                FileChannel fileChannel = fis.getChannel();
                fileChannel.write(buffer);
                buffer.flip();
                SocketChannel socketChannel = client.getChannel();
                while (buffer.hasRemaining()) {
                    socketChannel.write(buffer);
                }
                return;
            } catch (Exception e) {
                log.error("Write file to socket channel failed", e);
            }
        }
        headers.put(HttpHeader.CONTENT_LENGTH, "0");
        client.send(resp.toString().getBytes());
    }
    
    /**
     * 读取*.jar.sha1文件中的内容
     */
    private String readSHA1Str() {
        String respFilePath = respFile.getAbsolutePath();
        File respSHAFile = new File(respFilePath + SHA_FILE_SUFFIX);
        if (respSHAFile.exists()) {
            try (FileReader fr = new FileReader(respSHAFile); BufferedReader br = new BufferedReader(fr)) {
                return br.readLine();
            } catch (Exception e) {
                log.error("Read sha1 file failed!", e);
            }
        }
        String sha1 = SHAUtils.SHA1(respFile);
        try {
            respSHAFile.createNewFile();
            try (FileOutputStream fos = new FileOutputStream(respSHAFile)) {
                fos.write(sha1.getBytes());
            } catch (Exception e) {
                log.error("Write sha1 file failed", e);
                respSHAFile.delete();
            }
        } catch (IOException e) {
            log.error("Create sha1 file failed", e);
        }
        return sha1;
    }
    
    public String getProtocol() {
        return protocol;
    }
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }
    public Integer getHttpCode() {
        return httpCode;
    }
    public void setHttpCode(Integer httpCode) {
        this.httpCode = httpCode;
    }
    public String getHttpMsg() {
        return httpMsg;
    }
    public void setHttpMsg(String httpMsg) {
        this.httpMsg = httpMsg;
    }
    public HttpHeader getHeaders() {
        return headers;
    }
    public void setHeaders(HttpHeader headers) {
        this.headers = headers;
    }
    public File getRespFile() {
        return respFile;
    }
    public void setRespFile(File respFile) {
        this.respFile = respFile;
    }
}
