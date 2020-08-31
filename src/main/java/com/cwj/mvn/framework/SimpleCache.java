package com.cwj.mvn.framework;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 简单的缓存类
 */
public class SimpleCache {
    
    private static final ConcurrentHashMap<String, Entity> cacheMap = new ConcurrentHashMap<>();
    
    private static final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(); // 计划线程池, 用于清除过期数据

    private SimpleCache() {}
    
    /**
     * 添加缓存
     */
    public static void put(String key, Object value) {
        SimpleCache.put(key, value, 0);
    }

    /**
     * 添加缓存
     * @param expire 过期时间, 毫秒, 0或负数表示无限长
     */
    public static void put(String key, Object value, long expire) {
        put(key, value, expire, null);
    }
    
    /**
     * 添加缓存
     * @param expire 过期时间, 毫秒, 0或负数表示无限长
     * @param expireCallback 过期回调接口
     */
    public static void put(String key, Object value, long expire, ExpireCallback expireCallback) {
      //清除原键值对
        SimpleCache.remove(key);
        //设置过期时间
        if (expire > 0) {
            Future<?> future = executor.schedule(() -> {
                Entity entity = cacheMap.remove(key);
                if (entity != null && entity.expireCallback != null) 
                    entity.expireCallback.callback(key, value);
            }, expire, TimeUnit.MILLISECONDS);
            SimpleCache.put(key, new Entity(value, future, expireCallback));
        } else {
            //不设置过期时间
            SimpleCache.put(key, new Entity(value, null));
        }
    }
    
    /**
     * 读取缓存
     */
    @SuppressWarnings("unchecked")
    public static <T> T get(String key) {
        Entity entity = cacheMap.get(key);
        return entity == null ? null : (T) entity.value;
    }

    /**
     * 清除缓存
     */
    @SuppressWarnings("unchecked")
    public static <T> T remove(String key) {
        //清除原缓存数据
        Entity entity = cacheMap.remove(key);
        if (entity == null) {
            return null;
        }
        //清除原键值对定时器
        if (entity.future != null) {
            entity.future.cancel(true);
        }
        return (T) entity.value;
    }

    /**
     * 查询当前缓存的键值对数量
     */
    public static int size() {
        return cacheMap.size();
    }
    
    private static void put(String key, Entity entity) {
        cacheMap.put(key, entity);
    }
    
    private static class Entity {
        private Object value;
        
        private Future<?> future;
        
        private ExpireCallback expireCallback;
        
        private Entity(Object value, Future<?> future) {
            this(value, future, null);
        }
        
        private Entity(Object value, Future<?> future, ExpireCallback expireCallback) {
            this.value = value;
            this.future = future;
            this.expireCallback = expireCallback;
        }
    }
    
    public static interface ExpireCallback {
        void callback(String key, Object value);
    }
}
