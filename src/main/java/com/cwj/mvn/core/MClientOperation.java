package com.cwj.mvn.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.cwj.mvn.constant.Constant;
import com.cwj.mvn.framework.http.HttpMsg;
import com.cwj.mvn.framework.http.bean.HttpHeader;
import com.cwj.mvn.framework.http.bean.HttpParameter;
import com.cwj.mvn.framework.http.bean.HttpResponse;
import com.cwj.mvn.framework.socket.AbstractClientSocket;
import com.cwj.mvn.framework.socket.AbstractOperation;

public abstract class MClientOperation extends AbstractOperation<byte[]> {
    
    protected static final String SHA_FILE_SUFFIX = ".sha1";
    
    protected static final String HTTP_REQUEST = "HttpRequest";

    protected void returnHtml(String protocol, String html, HttpMsg httpMsg, AbstractClientSocket<byte[]> client) {
        HttpResponse resp = new HttpResponse(protocol, httpMsg);
        HttpHeader headers = new HttpHeader();
        headers.put(HttpHeader.CONNECTION, HttpHeader.KEEP_ALIVE);
        headers.put(HttpHeader.LAST_MODIFIED, Constant.LAST_MODIFIED);
        headers.put(HttpHeader.CONTENT_TYPE, HttpHeader.TYPE_HTML);
        headers.put(HttpHeader.ETAG, getSha1ByByte(html.getBytes()));
        resp.setHeaders(headers);
        HttpParameter param = new HttpParameter();
        param.put(HttpParameter.DATA, html);
        resp.setParameters(param);
        resp.send(client);
    }
    
    /**
     * 读取*.jar.sha1文件中的内容
     */
    protected String readSHA1Str(File jarFile) {
        String respFilePath = jarFile.getAbsolutePath();
        File respSHAFile = new File(respFilePath + SHA_FILE_SUFFIX);
        if (respSHAFile.exists()) {
            try (FileReader fr = new FileReader(respSHAFile); BufferedReader br = new BufferedReader(fr)) {
                return br.readLine();
            } catch (Exception e) {
                log.error("Read sha1 file failed!", e);
            }
        }
        return getSha1ByFile(jarFile);
    }
    
    /**
     * 根据jar包输入流获取该文件的sha1码
     * @param file
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    protected String getSha1ByFile(File file) {
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
    protected String getSha1ByByte(byte[] data) {
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
}
