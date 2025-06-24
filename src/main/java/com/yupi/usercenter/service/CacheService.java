package com.yupi.usercenter.service;


import java.time.Duration;
import java.util.function.Supplier;

/**
 * @author lipeng
 * @description 缓存服务
 * @since 2025/6/24 8:20
 */
public interface CacheService {

    Duration DEFAULT_CACHE_DURATION = Duration.ofHours(8);

    /**
     * 通用缓存获取方法
     * @param cacheKey 缓存键
     * @param supplier 数据供应商（缓存未命中时的回调）
     * @param expireDuration 过期时间
     * @return 缓存数据或从供应商获取的数据
     */
    <T> T getWithCache(String cacheKey, Supplier<T> supplier, Duration expireDuration);

    <T> void asyncUpdateCache(String cacheKey, T data, Duration expireDuration);
}
