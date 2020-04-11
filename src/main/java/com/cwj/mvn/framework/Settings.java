package com.cwj.mvn.framework;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cwj.mvn.utils.StringUtils;

public class Settings {
    
    private static final Logger log = LoggerFactory.getLogger(Settings.class);
    
    public static final String LOCAL_URL_SUFFIX = "localServerURLSuffix";
    public static final String LOCAL_REPOSITORY = "localRepository";
    public static final String LOCAL_PORT = "localPort";
    public static final String REMOTE_URL = "remoteURL";
    
    private static final String DEFAULT_SETTINGS_NAME = "default-settings.xml";
    private static final String SETTINGS_NAME = "settings.xml";
    private static final HashMap<String, String> settingMap = new HashMap<>();
    private static final HashMap<String, String> defaultSettingMap = new HashMap<>();
    
    /**
     * 加载settings文件
     */
    public static void loadSettings() {
        File settingsFile = new File(System.getProperty("user.dir") + File.separator + SETTINGS_NAME);
        if (settingsFile.exists()) {
            log.info("Read {} file", SETTINGS_NAME);
            try {
                readToMap(new FileInputStream(settingsFile), settingMap);
            } catch (FileNotFoundException e) {}
        }
        InputStream is = Settings.class.getResourceAsStream("/" + DEFAULT_SETTINGS_NAME);
        if (is != null) {
            log.info("Read {} file", DEFAULT_SETTINGS_NAME);
            readToMap(is, defaultSettingMap);
        }
    }
    
    /**
     * 获取配置
     */
    public static String getSetting(String key) {
        String value = null;
        if (settingMap.size() > 0) value = settingMap.get(key);
        return StringUtils.isBlank(value) ? defaultSettingMap.get(key) : value;
    }
    
    /**
     * 从XML读取配置到HashMap
     */
    private static void readToMap(InputStream is, HashMap<String, String> settingMap) {
        try {
            Document settings = new SAXReader().read(is);
            Element root = settings.getRootElement();
            for (Object children : root.elements()) {
                Element childEle = (Element) children;
                String name = childEle.getName();
                String value = childEle.getText();
                settingMap.put(name, value);
            }
        } catch (Exception e) {
            log.error("Read to map failed!", e);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                log.error("Close setting inputstream failed", e);
            }
        }
    }
}
