package com.cwj.mvn;

import com.cwj.mvn.framework.Settings;
import com.cwj.mvn.framework.ThreadPool;

public class MainActive {
    
    public static void main(String[] args) {
        Settings.loadSettings();
        ThreadPool.start();
    }
}