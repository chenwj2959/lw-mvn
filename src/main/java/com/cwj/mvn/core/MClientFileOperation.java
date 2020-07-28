package com.cwj.mvn.core;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.Date;
import java.util.HashMap;

import com.cwj.mvn.constant.Constant;
import com.cwj.mvn.framework.Settings;
import com.cwj.mvn.framework.http.HttpMsg;
import com.cwj.mvn.framework.http.bean.HttpHeader;
import com.cwj.mvn.framework.http.bean.HttpParameter;
import com.cwj.mvn.framework.http.bean.HttpRequest;
import com.cwj.mvn.framework.http.bean.HttpResponse;
import com.cwj.mvn.framework.socket.AbstractClientSocket;
import com.cwj.mvn.utils.DateUtils;
import com.cwj.mvn.utils.FileUtils;
import com.cwj.mvn.utils.HttpUtils;

public class MClientFileOperation extends MClientOperation {
    
    private static final String REMOTE_URL = Settings.getSetting(Settings.REMOTE_URL);

    @Override
    public Boolean handle(byte[] message, HashMap<String, Object> paramMap, AbstractClientSocket<byte[]> client) {
        try {
            HttpRequest request = new HttpRequest(message);
            String path = request.getPath();
            if (isHtmlReq(path)) {
                paramMap.put(HTTP_REQUEST, request);
                return nextHandler(message, paramMap, client);
            }
            String protocol = request.getProtocol();
            String contentType = getContentType(path);
            File resource = new File(LOCAL_REPOSITORY + path);
            if (returnFileIfExists(resource, contentType, protocol, client)) return true; // 本地已有, 直接返回
            // 从远程仓库下载
            HttpURLConnection conn = request.toHttp(REMOTE_URL);
            return returnByRemote(conn, request, resource, client);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return true;
    }
    
    private boolean returnByRemote(HttpURLConnection conn, HttpRequest request, File resource, AbstractClientSocket<byte[]> client) {
        try {
            conn.connect();
            int respCode = conn.getResponseCode();
            log.info("HTTP CODE {} {}", respCode, conn.getResponseMessage());
            String protocol = request.getProtocol();
            if (respCode == HttpURLConnection.HTTP_OK) {
                int length = conn.getHeaderFieldInt(HttpHeader.CONTENT_LENGTH, 0);
                if (length == 0) { // 没有文件返回
                    returnHtml(protocol, Constant.HTML_404, HttpMsg.NOT_FOUND, client);
                    return false;
                }
                InputStream is = conn.getInputStream();
                try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                    byte[] buffer = new byte[4096];
                    int ans;
                    while ((ans = is.read(buffer)) != -1) baos.write(buffer, 0, ans);
                    String respType = conn.getHeaderField(HttpHeader.CONTENT_TYPE);
                    if (FileUtils.write(baos.toByteArray(), resource)) {
                        return returnFileIfExists(resource, respType, protocol, client);
                    } else {
                        // 文件保存失败处理
                        return returnByByte(baos.toByteArray(), respType, protocol, client);
                    }
                }
            } else {
                // 403 aliyun镜像重定向, 301 重定向code
                String redirectUrl = conn.getHeaderField(HttpHeader.LOCATION);
                if (redirectUrl == null) { // 没有重定向连接
                    returnHtml(protocol, Constant.HTML_404, HttpMsg.NOT_FOUND, client);
                    return false;
                }
                log.info("Redire {}", redirectUrl);
                HttpURLConnection redireConn = HttpUtils.getConn(redirectUrl, request.getMethod(), request.getHeaders(), request.getParameters());
                return returnByRemote(redireConn, request, resource, client);
            }
        } catch (Exception e) {
            log.error("Return from remote failed!", e);
            return false;
        }
    }
    
    /**
     * 如果文件存在则返回
     */
    private boolean returnFileIfExists(File file, String contentType,  String protocol, AbstractClientSocket<byte[]> client) {
        if (file.exists()) { // 服务器已有，直接返回
            HttpResponse resp = new HttpResponse(protocol, HttpMsg.OK);
            HttpHeader headers = resp.getHeaders();
            headers.put(HttpHeader.CONTENT_TYPE, contentType);
            String etag = "\"" + readSHA1Str(file) + "\"";
            if (etag != null) headers.put(HttpHeader.ETAG, etag);
            headers.put(HttpHeader.LAST_MODIFIED, DateUtils.dateToString(file.lastModified(), DateUtils.EdMyHms_GMT));
            try (FileInputStream fis = new FileInputStream(file)) {
                HttpParameter parameters = resp.getParameters();
                if (parameters == null) {
                    parameters = new HttpParameter();
                    resp.setParameters(parameters);
                }
                int total = fis.available();
                byte[] buffer = new byte[total];
                int offset = 0;
                while (offset < total) {
                    offset += fis.read(buffer, offset, total - offset);
                }
                parameters.put(HttpParameter.DATA, buffer);
                
                resp.send(client);
                return true;
            } catch (Exception e) {
                log.error("Return response failed!", e);
                return false;
            }
        }
        return false;
    }
    
    /**
     * 直接返回remote仓库下载的byte
     */
    private boolean returnByByte(byte[] jarBuffer, String contentType, String protocol, AbstractClientSocket<byte[]> client) {
        HttpResponse resp = new HttpResponse(protocol, HttpMsg.OK);
        HttpHeader headers = resp.getHeaders();
        String etag = "\"" + getSha1ByByte(jarBuffer) + "\"";
        if (etag != null) headers.put(HttpHeader.ETAG, etag);
        headers.put(HttpHeader.LAST_MODIFIED, DateUtils.dateToString(new Date(), DateUtils.EdMyHms_GMT));
        headers.put(HttpHeader.CONTENT_TYPE, contentType);
        HttpParameter parameters = resp.getParameters();
        if (parameters == null) {
            parameters = new HttpParameter();
            resp.setParameters(parameters);
        }
        parameters.put(HttpParameter.DATA, jarBuffer);
        resp.send(client);
        return true;
    }
    
    /**
     * 根据route获取对应的contentType
     */
    private String getContentType(String route) {
        return route.endsWith(".jar") ? HttpHeader.TYPE_JAVA_ARCHIVE : HttpHeader.TYPE_XML;
    }
    
    /**
     * 是否为html请求
     */
    private boolean isHtmlReq(String route) {
        return !(route.endsWith(".jar") || route.endsWith(SHA_FILE_SUFFIX) || route.endsWith(".pom"));
    }
}
