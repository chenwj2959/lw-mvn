package com.cwj.mvn.core;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.Date;
import java.util.HashMap;

import com.cwj.mvn.constant.Constant;
import com.cwj.mvn.framework.http.HttpMsg;
import com.cwj.mvn.framework.http.bean.HttpHeader;
import com.cwj.mvn.framework.http.bean.HttpParameter;
import com.cwj.mvn.framework.http.bean.HttpRequest;
import com.cwj.mvn.framework.http.bean.HttpResponse;
import com.cwj.mvn.framework.socket.AbstractClientSocket;
import com.cwj.mvn.utils.DateUtils;
import com.cwj.mvn.utils.FileUtils;
import com.cwj.mvn.utils.HttpUtils;
import com.cwj.mvn.utils.StringUtils;

public class MClientFileOperation extends MClientOperation {
    
    @Override
    public Boolean handle(byte[] message, HashMap<String, Object> paramMap, AbstractClientSocket<byte[]> client) {
        try {
            HttpRequest request = new HttpRequest(message);
            String path = request.getPath();
            if (isHtmlReq(path)) {
                paramMap.put(HTTP_REQUEST, request);
                return nextHandler(message, paramMap, client);
            }
            File file = new File(Constant.LOCAL_REPOSITORY + path);
            if (returnFileIfExists(file, request.getProtocol(), client, null, null)) return true; // 本地已有, 直接返回
            // 从远程仓库下载
            HttpURLConnection conn = request.toHttp(Constant.REMOTE_URL);
            return returnByRemote(conn, request, file, client);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return true;
    }
    
    private boolean returnByRemote(HttpURLConnection conn, HttpRequest request, File file, AbstractClientSocket<byte[]> client) {
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
                    if (!FileUtils.write(baos.toByteArray(), file)) {
                        // 文件保存失败处理
                        String respType = getContentType(file.getName());
                        return returnByByte(baos.toByteArray(), respType, protocol, client);
                    }
                }
                String lastModify = conn.getHeaderField(HttpHeader.LAST_MODIFIED);
                file.setLastModified(DateUtils.stringToTimestamp(lastModify, DateUtils.EdMyHms_GMT));
                String etag = conn.getHeaderField(HttpHeader.ETAG);
                return returnFileIfExists(file, protocol, client, etag, lastModify);
            } else {
                // 301 重定向处理
                String redirectUrl = conn.getHeaderField(HttpHeader.LOCATION);
                if (redirectUrl == null) { // 没有重定向连接
                    returnHtml(protocol, Constant.HTML_404, HttpMsg.NOT_FOUND, client);
                    return false;
                }
                log.info("Redire {}", redirectUrl);
                HttpURLConnection redireConn = HttpUtils.getConn(redirectUrl, request.getMethod(), request.getHeaders(), request.getParameters());
                return returnByRemote(redireConn, request, file, client);
            }
        } catch (Exception e) {
            log.error("Return from remote failed!", e);
            return false;
        }
    }
    
    /**
     * 如果文件存在则返回
     */
    private boolean returnFileIfExists(File file, String protocol, AbstractClientSocket<byte[]> client, String etag, String lastModify) {
        if (file.exists()) { // 服务器已有，直接返回
            HttpResponse resp = new HttpResponse(protocol, HttpMsg.OK);
            HttpHeader headers = resp.getHeaders();
            headers.put(HttpHeader.CONTENT_TYPE, getContentType(file.getName()));
            if (StringUtils.isBlank(etag)) etag = getMD5ByFile(file);
            if (etag != null) {
                headers.put(HttpHeader.X_CHECKSUM_MD5, setMarks(etag, false));
                headers.put(HttpHeader.ETAG, setMarks(etag, true));
            }
            String sha1 = readSHA1Str(file);
            if (!StringUtils.isBlank(sha1)) headers.put(HttpHeader.X_CHECKSUM_SHA, sha1);
            if (StringUtils.isBlank(lastModify)) lastModify = DateUtils.dateToString(file.lastModified(), DateUtils.EdMyHms_GMT);
            headers.put(HttpHeader.LAST_MODIFIED, lastModify);
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
        String etag = getMD5ByByte(jarBuffer);
        if (etag != null) {
            headers.put(HttpHeader.X_CHECKSUM_MD5, setMarks(etag, false));
            headers.put(HttpHeader.ETAG, setMarks(etag, true));
        }
        String sha1 = getSha1ByByte(jarBuffer);
        if (!StringUtils.isBlank(sha1)) headers.put(HttpHeader.X_CHECKSUM_SHA, sha1);
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
     * Set double quotation marks
     */
    private String setMarks(String etag, boolean hasMark) {
        if (hasMark) {
            if (etag.startsWith("\"")) return etag;
            return "\"" + etag + "\"";
        } else {
            if (etag.startsWith("\"")) return etag.replaceAll("\"", "");
            return etag;
        }
    }
    
    /**
     * 根据route获取对应的contentType
     */
    private String getContentType(String route) {
        return route.endsWith(".jar") ? HttpHeader.TYPE_JAVA_ARCHIVE : HttpHeader.TYPE_TEXT_XML;
    }
    
    /**
     * 是否为html请求
     */
    private boolean isHtmlReq(String route) {
        return !(route.contains(".jar") || route.contains(SHA_FILE_SUFFIX) || route.contains(".pom"));
    }
}
