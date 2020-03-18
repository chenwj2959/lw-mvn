package com.cwj.mvn.framework;

public abstract class BaseRunnable implements Runnable {

    private String threadName;
    private boolean run;
    
    public BaseRunnable(String threadName) {
        this.threadName = threadName;
    }
    
    @Override
    public void run() {
        Thread.currentThread().setName(threadName);
        beforeHandler();
        handler();
    }
    
    public void beforeHandler() {}
    
    public abstract void handler();
    
    public void close() {
        this.run = false;
    }
}
