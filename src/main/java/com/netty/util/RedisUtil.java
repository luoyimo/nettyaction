package com.netty.util;

import org.redisson.Redisson;
import org.redisson.api.RBucket;
import org.redisson.api.RLock;
import org.redisson.api.RSet;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

import java.util.Properties;
import java.util.concurrent.TimeUnit;


/**
 * @Author hu
 * @Description: redis操作工具类
 * @Date Create In 14:33 2018/10/30 0030
 */
public class RedisUtil {

    private static RedissonClient redisson;

    static {
        Properties properties = PropertiesUtil.loadProperties();
        String redis_address = properties.getProperty("redis.address");
        String redis_pwd = properties.getProperty("redis.password");
        Integer timeOut = Integer.parseInt(properties.getProperty("redis.timeout"));
        Config config = new Config();
        config.useSingleServer().setAddress(redis_address);
        config.useSingleServer().setPassword(redis_pwd);
        config.useSingleServer().setTimeout(timeOut);
        redisson = Redisson.create(config);
    }

    /**
     * 缓存对象信息
     *
     * @param key
     * @param value
     */
    public static void addObject(String key, RBucket<Object> value) {
        RBucket<Object> bucket = redisson.getBucket(key);
        bucket.set(value);
    }

    /**
     * 将某个值加入set
     *
     * @param key
     * @param value
     */
    public static void addToSet(String key, String value) {
        RSet<String> set = redisson.getSet(key);
        set.add(value);
    }

    /**
     * 从set中移除某个值
     *
     * @param key
     * @param value
     */
    public static void removeFromSet(String key, String value) {
        RSet<String> set = redisson.getSet(key);
        set.remove(value);
    }

    /**
     * 获取set
     *
     * @param key
     */
    public static RSet<String> getSet(String key) {
        RSet<String> set = redisson.getSet(key);
        return set;
    }


    /**
     * 缓存一个String
     *
     * @param key
     * @param value
     */
    public static void setStr(String key, String value) {
        RBucket<Object> bucket = redisson.getBucket(key);
        bucket.set(value);
    }

    /**
     * 缓存一个String带超时时间
     *
     * @param key
     * @param value
     * @param liveSeconds 存活时间
     */
    public static void setStr(String key, String value, long liveSeconds) {
        RBucket<Object> bucket = redisson.getBucket(key);
        bucket.set(value, liveSeconds, TimeUnit.SECONDS);
    }

    /**
     * 缓存一个String 在
     *
     * @param key
     * @param value
     * @param timestamp 超时时间错
     */
    public static void setStrAndTimestamp(String key, String value, long timestamp) {
        RBucket<Object> bucket = redisson.getBucket(key);
        bucket.set(value);
        bucket.expireAt(timestamp);
    }

    /**
     * 获取缓存对象
     *
     * @param key
     * @return
     */
    public static Object getObject(String key) {
        RBucket<Object> bucket = redisson.getBucket(key);
        Object obj = bucket.get();
        return obj;
    }

    /**
     * 获取一个缓存的String
     *
     * @param key
     * @return
     */
    public static String getStr(String key) {
        RBucket<String> bucket = redisson.getBucket(key);
        String obj = bucket.get();
        return obj;
    }

    /**
     * 获取一个删除的String
     *
     * @param key
     * @return
     */
    public static boolean removeStr(String key) {
        RBucket<String> bucket = redisson.getBucket(key);
        return bucket.delete();
    }

    /**
     * 获取某个锁
     *
     * @param key
     * @return
     */
    public static RLock getLock(String key) {
        RLock lock = redisson.getLock(key);
        return lock;
    }


    /**
     * 关闭Redisson客户端连接
     */
    public static void closeRedisson() {
        redisson.shutdown();
    }

}
