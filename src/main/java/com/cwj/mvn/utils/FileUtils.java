package com.cwj.mvn.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileUtils {
    
    /**
     * 如果文件不存在, 创建文件及父文件夹
     */
    private static boolean create(File file) throws IOException {
        if (file.exists()) return true;
        if (file.isDirectory()) return file.mkdirs();
        File parent = file.getParentFile();
        if (!parent.exists()) 
            if (!parent.mkdirs()) return false;
        if (!file.createNewFile()) return false;
        return true;
    }
    
    /**
     * 保存成文件, 如果buffer == null, 则生成一个空白文件
     */
    public static boolean write(byte[] buffer, String path) throws IOException {
        if (path == null) return false;
        return write(buffer, new File(path));
    }
    
    /**
     * 保存成文件, 如果buffer == null, 则生成一个空白文件
     */
    public static boolean write(byte[] buffer, File file) throws IOException {
        if (!create(file)) return false;
        if (buffer == null) return true;
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(buffer);
            fos.flush();
            return true;
        }
    }

    /**
     * 读取文件
     */
    public static byte[] read(String path) throws IOException {
        return read(new File(path));
    }
    
    public static byte[] read(File file) throws IOException {
        if (!file.exists()) throw new FileNotFoundException();
        try (FileInputStream fis = new FileInputStream(file)) {
            int total = fis.available();
            byte[] buffer = new byte[total];
            int ans = 0;
            while (ans < total) ans += fis.read(buffer, ans, total - ans);
            return buffer;
        }
    }
}
