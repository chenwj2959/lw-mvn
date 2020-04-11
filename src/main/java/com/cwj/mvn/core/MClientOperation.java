package com.cwj.mvn.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
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
import com.cwj.mvn.utils.SHAUtils;

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
                MClientSocket mClient = new MClientSocket.ClientBuilder()
                        .domain(getHost())
                        .connectTimeout(5000)
                        .tag(route)
                        .build(MClientSocket.class);
                mClient.connection();
                mClient.send(message);
                byte[] buffer = mClient.receive();
                HttpResponse resp = HttpResponse.parse(buffer);
                HttpMsg httpMsg = resp.getHttpMsg();
                if (httpMsg.code() == 200) {
//                    HttpHeader headers = resp.getHeaders();
//                    String contentType = headers.get(HttpHeader.CONTENT_TYPE);
                    HttpParameter parameters = resp.getParameters();
                    byte[] jarBuffer = parameters.getData(contentType);
                    if (writeJarFile(resource, jarBuffer)) {
                        writeSHA1File(resource);
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
            headers.put(HttpHeader.ETAG, readSHA1Str(file));
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
            } catch (Exception e) {
                log.error("Return response failed!", e);
            }
            return true;
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
        headers.put(HttpHeader.ETAG, SHAUtils.SHA1(jarBuffer));
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
        String sha1 = SHAUtils.SHA1(jarFile);
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
     * 保存buffer到jar文件
     */
    private boolean writeJarFile(File jarFile, byte[] buffer) {
        try (FileOutputStream fos = new FileOutputStream(jarFile)) {
            fos.write(buffer);
            fos.flush();
            return true;
        } catch (Exception e) {
            log.error("Write jar file failed!", e);
            return false;
        }
    }
    
    /**
     * 根据jar文件，生成sha1写入文件
     */
    private void writeSHA1File(File jarFile) {
        if (jarFile.exists()) {
            String sha1Str = SHAUtils.SHA1(jarFile);
            String jarFilePath = jarFile.getAbsolutePath();
            try (FileOutputStream fos = new FileOutputStream(jarFilePath + SHA_FILE_SUFFIX)) {
                fos.write(sha1Str.getBytes());
                fos.flush();
            } catch (Exception e) {
                log.error("Write sha1 file failed! jar path is " + jarFilePath, e);
            }
        }
    }
    
    /**
     * 获取remote host
     */
    private String getHost() throws URISyntaxException {
        URI url = new URI(REMOTE_URL);
        return url.getHost();
    }
    
    /**
     * 根据route获取对应的contentType
     */
    private String getContentType(String route) {
        return route.endsWith(".jar") ? HttpHeader.TYPE_JAVA_ARCHIVE : HttpHeader.TYPE_XML;
    }
}
