package com.cwj.mvn.framework;

import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

public abstract class BaseRunnable implements Runnable {
    
    protected static final Logger log = LoggerFactory.getLogger(BaseRunnable.class);
    protected boolean run;
    private static final String LOG_NAME = "logName";

    public String TAG; // 线程池名 - 存储在线程池中的name，唯一不可重复

    public BaseRunnable(String TAG) {
        this.TAG = TAG;
        this.run = true;
    }
    
    @Override
    public final void run() {
        MDC.put(LOG_NAME, TAG);
        beforeLoop();
        loop();
        close();
        removeThread();
        log.info(TAG + " thread closed");
    }
    
    public void beforeLoop() {}

    public abstract void loop();

    /**
     * 从线程池和heartbeat服务中移除当前服务
     * <br/>
     * 不会中断线程和IO
     */
    public void close() {
        close(false);
    }

    /**
     * 从线程池和heartbeat服务中移除当前服务
     * @param mayInterruptIfRunning 是否中断线程阻塞
     */
    public void close(boolean mayInterruptIfRunning) {
        if (closed()) return;
        this.run = false;
        if (mayInterruptIfRunning) {
            Future<?> future = ThreadPool.getFuture(TAG);
            if (future == null){
                log.error("Cannot found {} future in thread pool", TAG);
                return;
            }
            future.cancel(true);
        }
    }

    public boolean closed() {
        return !this.run;
    }
    
    public boolean removeThread() {
        if (ThreadPool.contains(TAG)) return ThreadPool.removeServer(TAG);
        return true;
    }
}