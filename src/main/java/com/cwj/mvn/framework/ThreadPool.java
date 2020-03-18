package com.cwj.mvn.framework;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.sun.corba.se.impl.orbutil.closure.Future;

public class ThreadPool {

    // 存放线程状态对象
    private static final ConcurrentHashMap<String, Future> futrueMap = new ConcurrentHashMap<>();
    
    // 存放线程
    private static final ConcurrentHashMap<String, BaseRunnable> threadMap = new ConcurrentHashMap<>();
}
