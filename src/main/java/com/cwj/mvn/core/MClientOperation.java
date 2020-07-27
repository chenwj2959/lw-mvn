package com.cwj.mvn.core;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
import com.cwj.mvn.framework.socket.AbstractOperation;
import com.cwj.mvn.utils.DateUtils;
import com.cwj.mvn.utils.FileUtils;
import com.cwj.mvn.utils.HttpUtils;

public class MClientOperation extends AbstractOperation<byte[]> {
    
    private static final String LOCAL_REPOSITORY = Settings.getSetting(Settings.LOCAL_REPOSITORY);
    private static final String REMOTE_URL = Settings.getSetting(Settings.REMOTE_URL);
    
    private static final String SHA_FILE_SUFFIX = ".sha1";

    @Override
    public Boolean handle(byte[] message, HashMap<String, Object> paramMap, AbstractClientSocket<byte[]> client) {
        try {
            HttpRequest request = new HttpRequest(message);
            String path = request.getPath();
            String protocol = request.getProtocol();
            String contentType = getContentType(path);
            File resource = new File(LOCAL_REPOSITORY + path);
            if (returnFileIfExists(resource, contentType, protocol, client)) return true; // 本地已有，直接返回
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
                if (length == 0) { // TODO 没有文件返回
                    returnNotFound(protocol, client);
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
                if (redirectUrl == null) { // TODO 没有重定向连接
                    returnNotFound(protocol, client);
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
    
    private void returnNotFound(String protocol, AbstractClientSocket<byte[]> client) {
        HttpResponse resp = new HttpResponse(protocol, HttpMsg.NOT_FOUND);
        HttpHeader headers = new HttpHeader();
        headers.put(HttpHeader.CONNECTION, HttpHeader.KEEP_ALIVE);
        headers.put(HttpHeader.LAST_MODIFIED, Constant.LAST_MODIFIED);
        headers.put(HttpHeader.ETAG, getSha1ByByte(Constant.HTML_404.getBytes()));
        headers.put(HttpHeader.CONTENT_TYPE, HttpHeader.TYPE_HTML);
//        headers.put("x-amz-error-code", "NoSuchKey");
//        headers.put("x-amz-error-message", "The specified key does not exist.");
//        headers.put("x-amz-error-detail-Key", path);
//        headers.put("X-Served-By", "cache-bwi5149-BWI, cache-sea4425-SEA");
//        headers.put("X-Cache", "MISS, HIT");
//        headers.put("X-Cache-Hits", "0, 1");
//        headers.put("X-Timer", "S1595850655.089785,VS0,VE1");
//        headers.put("Age", "808");
        resp.setHeaders(headers);
        HttpParameter param = new HttpParameter();
        param.put(HttpParameter.DATA, Constant.HTML_404);
        resp.send(client);
    }
    
    /**
     * 读取*.jar.sha1文件中的内容
     */
    private String readSHA1Str(File jarFile) {
        String respFilePath = jarFile.getAbsolutePath();
        File respSHAFile = new File(respFilePath + SHA_FILE_SUFFIX);
        if (respSHAFile.exists()) {
            try (FileReader fr = new FileReader(respSHAFile); BufferedReader br = new BufferedReader(fr)) {
                return br.readLine();
            } catch (Exception e) {
                log.error("Read sha1 file failed!", e);
            }
        }
        String sha1 = getSha1ByFile(jarFile);
        if (sha1 == null) return sha1;
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
    
    /**
     * 根据jar包输入流获取该文件的sha1码
     * @param file
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    private String getSha1ByFile(File file) {
        try (FileInputStream fis = new FileInputStream(file)) {
            MessageDigest sha1 = MessageDigest.getInstance("SHA1");
            byte[] data = new byte[1024];
            int read;
            while ((read = fis.read(data)) != -1) {
                sha1.update(data, 0, read);
            }
            byte[] hashBytes = sha1.digest();
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < hashBytes.length; i++) {
                sb.append(Integer.toString((hashBytes[i] & 0xff) + 0x100, 16).substring(1));
            }
            return sb.toString();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * 根据jar包二进制输入流获取该文件的sha1码
     * @param file
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    private String getSha1ByByte(byte[] data) {
        try {
            MessageDigest sha1 = MessageDigest.getInstance("SHA1");
            sha1.update(data, 0, data.length);
            byte[] hashBytes = sha1.digest();
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < hashBytes.length; i++) {
                sb.append(Integer.toString((hashBytes[i] & 0xff) + 0x100, 16).substring(1));
            }
            return sb.toString();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * 根据route获取对应的contentType
     */
    private String getContentType(String route) {
        return route.endsWith(".jar") ? HttpHeader.TYPE_JAVA_ARCHIVE : HttpHeader.TYPE_XML;
    }
}
