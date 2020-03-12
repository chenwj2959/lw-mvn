package com.cwj.mvn.core.pool;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.sun.corba.se.impl.orbutil.closure.Future;

public class ThreadPool {

    // 存放线程
    private static final ConcurrentHashMap<String, Future> threadMap = new ConcurrentHashMap<>();
    
    private static final ExecutorService pool = Executors.newWorkStealingPool()
}
