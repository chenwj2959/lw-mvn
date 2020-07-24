package com.cwj.mvn.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import com.cwj.mvn.framework.Settings;
import com.cwj.mvn.framework.ThreadPool;
import com.cwj.mvn.framework.http.HttpMsg;
import com.cwj.mvn.framework.http.bean.HttpHeader;
import com.cwj.mvn.framework.http.bean.HttpParameter;
import com.cwj.mvn.framework.http.bean.HttpRequest;
import com.cwj.mvn.framework.http.bean.HttpResponse;
import com.cwj.mvn.framework.socket.AbstractClientSocket;
import com.cwj.mvn.framework.socket.AbstractOperation;
import com.cwj.mvn.utils.DateUtils;
import com.cwj.mvn.utils.FileUtils;

public class MClientOperation extends AbstractOperation<byte[]> {
    
    private static final String LOCAL_REPOSITORY = Settings.getSetting(Settings.LOCAL_REPOSITORY);
    private static final String REMOTE_URL = Settings.getSetting(Settings.REMOTE_URL);
    
    private static final String SHA_FILE_SUFFIX = ".sha1";

    @Override
    public Boolean handle(byte[] message, HashMap<String, Object> paramMap, AbstractClientSocket<byte[]> client) {
        try {
            HttpRequest request = new HttpRequest(message);
            String route = request.getRoute(); 
            String contentType = getContentType(route);
            File resource = new File(LOCAL_REPOSITORY + route);
            if (returnFileIfExists(resource, contentType, request, client)) return true; // 本地已有，直接返回
            
            // 本地没有
            // 检测是否正在下载
            boolean downloaded = false;
            while (ThreadPool.contains(route)) { // 正在下载则等待
                downloaded = true;
                TimeUnit.SECONDS.sleep(1);
            }
            if (downloaded) { // 下载完成后返回
                if (!returnFileIfExists(resource, contentType, request, client)) {
                    // TODO 从远程仓库下载失败
                }
                return true;
            }
            
            // 从远程仓库下载
            try {
                URI url = new URI(REMOTE_URL);
                MClientSocket mClient = new MClientSocket.ClientBuilder()
                        .ip(url.getHost())
                        .port(url.getPort())
                        .connectTimeout(5000)
                        .tag(route)
                        .debug(false)
                        .build(MClientSocket.class);
                mClient.connection();
                mClient.send(message);
                byte[] buffer = mClient.receive();
                HttpResponse resp = HttpResponse.parse(buffer);
                HttpMsg httpMsg = resp.getHttpMsg();
                if (httpMsg.code() == 200) {
                    HttpHeader headers = resp.getHeaders();
                    int length = Integer.parseInt(headers.getOrDefault(HttpHeader.CONTENT_LENGTH, "0"));
                    if (length == 0) { // TODO 没有文件返回
                        return true;
                    }
                    HttpParameter parameters = resp.getParameters();
                    byte[] jarBuffer = parameters == null ? mClient.receive(length) : parameters.getData(contentType);
                    if (FileUtils.write(jarBuffer, resource)) {
                        returnFileIfExists(resource, contentType, request, client);
                    } else {
                        // 文件保存失败处理
                        returnByByte(jarBuffer, resp.getHeaders().get(HttpHeader.CONTENT_TYPE), request, client);
                    }
                }
            } catch (Exception e) {
                log.error("Return from remote failed!", e);
            }
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        }
        return true;
    }
    
    /**
     * 如果文件存在则返回
     */
    private boolean returnFileIfExists(File file, String contentType, HttpRequest request, AbstractClientSocket<byte[]> client) {
        if (file.exists()) { // 服务器已有，直接返回
            HttpResponse resp = new HttpResponse(request.getProtocol(), HttpMsg.OK);
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
     * @param jarBuffer
     * @param request
     * @param client
     * @return
     */
    private boolean returnByByte(byte[] jarBuffer, String contentType, HttpRequest request, AbstractClientSocket<byte[]> client) {
        HttpResponse resp = new HttpResponse(request.getProtocol(), HttpMsg.OK);
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
