package com.cwj.mvn.framework;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Settings {
    
    private static final Logger log = LoggerFactory.getLogger(Settings.class);
    
    public static final String URL_SUFFIX = "serverURLSuffix";
    public static final String REMOTE_URL = "remoteURL";
    public static final String LOCAL_REPOSITORY = "localRepository";
    
    private static final String DEFAULT_SETTINGS_NAME = "default-settings.xml";
    private static final String SETTINGS_NAME = "settings.xml";
    private static final HashMap<String, String> settingMap = new HashMap<>();
    
    /**
     * 加载settings文件
     */
    public static void loadSettings() throws Exception {
        File settingsFile = new File(System.getProperty("user.dir") + File.separator + SETTINGS_NAME);
        InputStream is = null;
        if (!settingsFile.exists()) {
            log.error("Cannot found setting.xml");
            is = Settings.class.getResourceAsStream(DEFAULT_SETTINGS_NAME);
        } else {
            is = new FileInputStream(settingsFile);
        }
        Document settings = new SAXReader().read(is);
        is.close();
        Element root = settings.getRootElement();
        for (Object children : root.elements()) {
            Element childEle = (Element) children;
            String name = childEle.getName();
            String value = childEle.getText();
            settingMap.put(name, value);
        }
    }
    
    /**
     * 获取配置
     */
    public static String getSetting(String key) {
        return settingMap.get(key);
    }
}
