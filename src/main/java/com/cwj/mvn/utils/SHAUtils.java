package com.cwj.mvn.utils;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class SHAUtils {
    private static final String CHARSET_UTF8 = "utf8";
    
    /**
     * 对byte[]进行SHA1摘要
     */
    public static String SHA1(final byte[] buffer) {
        return SHA(buffer, "SHA1");
    }
    
    /**
     * 对object进行SHA1摘要
     */
    public static String SHA1(final Object obj) {
        return SHA(objectToByte(obj), "SHA1");
    }
    
    /**
     * byte数组 SHA 加密
     */
    public static String SHA(byte[] data, final String algorithm) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance(algorithm);
            messageDigest.update(data);
            byte byteBuffer[] = messageDigest.digest();
            StringBuffer strHexString = new StringBuffer();
            for (int i = 0; i < byteBuffer.length; i++) {
                String hex = Integer.toHexString(0xff & byteBuffer[i]);
                if (hex.length() == 1) {
                    strHexString.append('0');
                }
                strHexString.append(hex);
            }
            return strHexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 使用 HMAC-SHA1 签名方法对data进行签名
     * @param data 被签名的字符串
     * @param key 密钥
     */
    public static String getHMAC(byte[] data, byte[] key) {
        try {
            // 根据给定的字节数组构造一个密钥,第二参数指定一个密钥算法的名称
            SecretKeySpec signinKey = new SecretKeySpec(key, "HmacSHA1");
            // 生成一个指定 Mac 算法 的 Mac 对象
            Mac mac = Mac.getInstance("HmacSHA1");
            // 用给定密钥初始化 Mac 对象
            mac.init(signinKey);
            // 完成 Mac 操作
            byte[] rawHmac = mac.doFinal(data);
            rawHmac = Base64.getEncoder().encode(rawHmac);
            return new String(rawHmac, CHARSET_UTF8);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
    
    /**
     * object转byte[]
     */
    private static byte[] objectToByte(final Object obj) {
        if (obj == null) return null;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(obj);
            return baos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
