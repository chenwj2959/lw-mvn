package com.cwj.mvn.framework;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;

public class ThreadPool {
    
    private static final Logger log = LoggerFactory.getLogger(ThreadPool.class);

    public static final String SIGN_CLOSED = "closed";
    public static final String SIGN_ACTIVE_COUNT = "activeCount";
    public static final String SIGN_ACTIVE_THREAD = "activeThread";

    // 线程池
    private static final ThreadPoolExecutor pool;
    // 存放所有在线程池中的Runnable
    private static final ConcurrentHashMap<String, BaseRunnable> runnableMap;
    // 存放所有在线程池中的线程状态
    private static final ConcurrentHashMap<String, Future<?>> futureMap;
    // 最多存储的线程状态数量
    private static final int MAXIMUNM_POOL_SIZE = 100;

    static {
        // 初始化参数
        int maxCoreThread = Runtime.getRuntime().availableProcessors() * 2 + 1;
        pool = new ThreadPoolExecutor(
                maxCoreThread, MAXIMUNM_POOL_SIZE,
                0L, TimeUnit.SECONDS,
                new SynchronousQueue<>());
        runnableMap = new ConcurrentHashMap<>();
        futureMap = new ConcurrentHashMap<>();
    }

    private ThreadPool() {}

    /**
     * 获取线程实例
     */
    public static BaseRunnable get(String key) {
        return runnableMap.get(key);
    }

    /**
     * 返回线程池中包含key的线程实例
     */
    public static List<BaseRunnable> find(String key) {
        List<BaseRunnable> list = new ArrayList<>();
        for (Map.Entry<String, BaseRunnable> runnables : runnableMap.entrySet()) {
            String tag = runnables.getKey();
            if (tag.contains(key)) list.add(runnables.getValue());
        }
        return list;
    }

    /**
     * 判断线程池中是否含有某个线程实例
     */
    public static boolean contains(String key) {
        return get(key) != null;
    }

    /**
     * 获取线程的状态对象
     */
    public static Future<?> getFuture(String key) {
        return futureMap.get(key);
    }

    /**
     * 返回线程池中包含key的线程状态对象
     */
    public static List<Future<?>> findFuture(String key) {
        List<Future<?>> list = new ArrayList<>();
        for (Map.Entry<String, Future<?>> futures : futureMap.entrySet()) {
            if (futures.getKey().contains(key)) list.add(futures.getValue());
        }
        return list;
    }

    /**
     * 返回该线程是否已经运行结束
     */
    public static boolean serverDone(String key) {
        Future<?> future = futureMap.get(key);
        return future == null || future.isDone();
    }

    /**
     * 运行线程并放入threadMap中
     */
    public static boolean putServer(BaseRunnable baseRunnable) {
        if (baseRunnable == null) throw new NullPointerException("BaseRunnable cannot be empty!");
        String key = baseRunnable.TAG;
        if (runnableMap.contains(key) || !serverDone(key)) {
            log.info("{} thread already run!", key);
            return false;
        }
        Future<?> future = pool.submit(baseRunnable);
        futureMap.put(key, future);
        runnableMap.put(key, baseRunnable);
        log.info("{} thread running", key);
        if (futureMap.size() > MAXIMUNM_POOL_SIZE) clearFuture();
        return true;
    }

    /**
     * 在线程循环结束后调用此方法从threadMap中移除线程
     */
    public static boolean removeServer(String key) {
        BaseRunnable baseRunnable = runnableMap.remove(key);
        if (baseRunnable == null) {
            log.error("Remove thread " + key + " failed, not found this key!");
        }
        Future<?> future = futureMap.remove(key);
        if (future != null && !future.isDone()) {
            boolean cancelFlag = future.cancel(true);
            log.info("Thread {} is running, cancel {}.", key, cancelFlag);
        }
        log.info("Active thread count = {}", pool.getActiveCount());
        return true;
    }

    /**
     * 清除futureMap中已完成的线程
     */
    private static void clearFuture() {
        Collection<Future<?>> futures = futureMap.values();
        Iterator<Future<?>> it = futures.iterator();
        while (it.hasNext()) {
            Future<?> future = it.next();
            if (future.isDone()) it.remove();
        }
    }

    /**
     * 开启默认服务
     */
    public boolean start() {
        JSONObject closeJson = closed();
        if (!closeJson.getBoolean(SIGN_CLOSED)) {
            log.error("Bridge is closed!");
            return false;
        }
        try {
            return true;
        } catch (Exception e) {
            log.error("Start bridge failed!", e);
            close();
            return false;
        }
    }

    /**
     * 调用threadMap中所有线程的close()方法
     */
    public void close() {
        log.info("Close bridge server start");
        for (Map.Entry<String, BaseRunnable> threads : runnableMap.entrySet()) {
            log.info("Closing " + threads.getKey() + " thread");
            threads.getValue().close();
        }
    }

    /**
     * 所有线程是否都已关闭
     * @return 仍未关闭的线程池名
     * {
     *     "closed": false,
     *     "activeCount": 3,
     *     "activeThread": "heartbeat,posServer,couldServer"
     * }
     */
    public JSONObject closed() {
        int activeCount = 0;
        StringBuilder activeThread = new StringBuilder();
        for (Map.Entry<String, Future<?>> futures : futureMap.entrySet()) {
            if (futures.getValue().isDone()) {
                futureMap.remove(futures.getKey());
            } else {
                activeCount++;
                activeThread.append(",").append(futures.getKey());
            }
        }
        JSONObject json = new JSONObject();
        if (activeCount == 0) {
            json.put(SIGN_CLOSED, true);
        } else {
            json.put(SIGN_CLOSED, false);
            json.put(SIGN_ACTIVE_COUNT, activeCount);
            json.put(SIGN_ACTIVE_THREAD, activeThread.substring(1));
        }
        return json;
    }

    /**
     * 更新线程名
     * @throws NullPointerException tag不存在
     * @throws RuntimeException newTag已存在
     */
    public boolean updateTag(String tag, String newTag) {
        if (contains(newTag)) {
            throw new RuntimeException("Update [" + tag + "] tag failed, New tag [" + newTag + "] is already exits");
        }
        BaseRunnable baseRunnable = runnableMap.remove(tag);
        if (baseRunnable == null) {
            throw new NullPointerException("Update [" + tag + "] tag failed, it's not exits");
        }
        runnableMap.put(newTag, baseRunnable);
        Future<?> future = futureMap.remove(tag);
        futureMap.put(newTag, future);
        log.info("Update [{}] tag to [{}] new tag done", tag, newTag);
        return true;
    }
}
