package com.cwj.mvn.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.MessageDigest;
import java.util.Date;

import com.cwj.mvn.constant.Constant;
import com.cwj.mvn.framework.SimpleCache;
import com.cwj.mvn.framework.http.HttpMsg;
import com.cwj.mvn.framework.http.bean.HttpHeader;
import com.cwj.mvn.framework.http.bean.HttpParameter;
import com.cwj.mvn.framework.http.bean.HttpResponse;
import com.cwj.mvn.framework.socket.AbstractClientSocket;
import com.cwj.mvn.framework.socket.AbstractOperation;
import com.cwj.mvn.utils.DateUtils;
import com.cwj.mvn.utils.FileUtils;
import com.cwj.mvn.utils.StringUtils;

public abstract class MClientOperation extends AbstractOperation<byte[]> {
    
    private static final String SHA1_FILE_SUFFIX = ".sha1";
    private static final String SHA1_SHA1_FILE_SUFFIX = ".sha1.sha1";
    private static final String MD5_FILE_SUFFIX = ".md5";
    private static final String POM_FILE_SUFFIX = ".pom";
    private static final String JAR_FILE_SUFFIX = ".jar";
    private static final String XML_FILE_SUFFIX = ".xml";
    
    protected static final String HTTP_REQUEST = "HttpRequest";
    
    protected static final String CLIENT_IP_PATH = Constant.ROOT + "ClientIPAddresses.txt";
    private static final int CLIENT_IP_EXPIRE = 180000;

    protected void returnHtml(String protocol, String html, HttpMsg httpMsg, AbstractClientSocket<byte[]> client) {
        HttpResponse resp = new HttpResponse(protocol, httpMsg);
        HttpHeader headers = new HttpHeader();
        headers.put(HttpHeader.CONTENT_TYPE, HttpHeader.TYPE_HTML);
        headers.put(HttpHeader.CACHE_CONTROL, HttpHeader.NO_CACHE);
        resp.setHeaders(headers);
        HttpParameter param = new HttpParameter();
        param.put(HttpParameter.DATA, html);
        resp.setParameters(param);
        resp.send(client);
    }
    
    /**
     * 根据文件获取该文件的sha1码
     */
    protected String getSha1ByFile(File file) {
        String sha1FileName = file.getName() + SHA1_FILE_SUFFIX;
        String sha1Str = SimpleCache.get(sha1FileName);
        if (!StringUtils.isEmpty(sha1Str)) return sha1Str;
        File sha1File = new File(file.getAbsolutePath() + SHA1_FILE_SUFFIX);
        if (sha1File.exists()) {
            sha1Str = readSha1ByFile(sha1File);
            SimpleCache.put(sha1FileName, sha1Str);
            return sha1Str;
        }
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
            sha1Str = sb.toString();
            SimpleCache.put(sha1FileName, sha1Str);
            FileUtils.write(sha1Str.getBytes(), sha1File);
            return sha1Str;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * 读取*.jar.sha1文件中的内容
     */
    private String readSha1ByFile(File file) {
        try (FileInputStream fis = new FileInputStream(file)) {
            int total = fis.available();
            byte[] buffer = new byte[total];
            int ans = 0;
            while (ans < total) ans += fis.read(buffer, ans, total - ans);
            return new String(buffer);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * 根据二进制流获取该文件的sha1码
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
    
    /**
     * 根据文件获取该文件的MD5值
     * 1. 先查询是否存在.md5文件
     * 2. 如果没有, 创建.md5文件
     */
    protected String getMD5ByFile(File file) {
        String md5FileName = file.getName() + MD5_FILE_SUFFIX;
        String md5Str = SimpleCache.get(md5FileName);
        if (!StringUtils.isEmpty(md5Str)) return md5Str;
        File md5File = new File(file.getAbsolutePath() + MD5_FILE_SUFFIX);
        if (md5File.exists()) {
            md5Str = readMD5ByFile(md5File);
            SimpleCache.put(md5FileName, md5Str);
            return md5Str;
        }
        try (FileInputStream fis = new FileInputStream(file)) {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            byte[] data = new byte[1024];
            int read;
            while ((read = fis.read(data)) != -1) {
                messageDigest.update(data, 0, read);
            }
            byte[] byteBuffer = messageDigest.digest();
            StringBuffer strHexString = new StringBuffer();
            for (int i = 0; i < byteBuffer.length; i++) {
                String hex = Integer.toHexString(0xff & byteBuffer[i]);
                if (hex.length() == 1) {
                    strHexString.append('0');
                }
                strHexString.append(hex);
            }
            md5Str = strHexString.toString();
            SimpleCache.put(md5FileName, md5Str);
            FileUtils.write(md5Str.getBytes(), md5File);
            return md5Str;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * 从.md5文件中读取md5
     */
    private String readMD5ByFile(File file) {
        try (FileInputStream fis = new FileInputStream(file)) {
            int total = fis.available();
            byte[] buffer = new byte[total];
            int ans = 0;
            while (ans < total) ans += fis.read(buffer, ans, total - ans);
            return new String(buffer);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * 根据二进制流获取该文件的MD5值
     */
    protected String getMD5ByByte(byte[] data) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.update(data, 0, data.length);
            byte[] byteBuffer = messageDigest.digest();
            StringBuffer strHexString = new StringBuffer();
            for (int i = 0; i < byteBuffer.length; i++) {
                String hex = Integer.toHexString(0xff & byteBuffer[i]);
                if (hex.length() == 1) {
                    strHexString.append('0');
                }
                strHexString.append(hex);
            }
            return strHexString.toString();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * 判断是否为文件
     */
    protected boolean isFile(String fileName) {
        return fileName.endsWith(SHA1_FILE_SUFFIX) || fileName.endsWith(POM_FILE_SUFFIX) || fileName.endsWith(JAR_FILE_SUFFIX) || fileName.endsWith(XML_FILE_SUFFIX);
    }
    
    /**
     * 是否为禁止访问的文件类型
     */
    protected boolean cannotAccess(String fileName) {
        return fileName.endsWith(MD5_FILE_SUFFIX) || fileName.endsWith(SHA1_SHA1_FILE_SUFFIX);
    }
    
    /**
     * 保存访客IP
     */
    protected void saveClientIP(AbstractClientSocket<byte[]> client) {
        String ip = client.getRemoteIp();
        Object count = SimpleCache.get(ip);
        SimpleCache.put(ip, count == null ? 1 : (int) count + 1, CLIENT_IP_EXPIRE, (key, value) -> {
            String now = DateUtils.dateToString(new Date(), DateUtils.yMdHmsS);
            String record = "[" + now + "] " + key + " - " + value + Constant.NEW_LINE;
            try (FileOutputStream fos = new FileOutputStream(CLIENT_IP_PATH, true)) {
                fos.write(record.getBytes());
                fos.flush();
            } catch (Exception e) {
                log.error("Save access IP record failed! " + record, e);
            }
        });
    }
}
