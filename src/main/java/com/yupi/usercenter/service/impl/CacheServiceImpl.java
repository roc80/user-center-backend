package com.yupi.usercenter.service.impl;

import com.yupi.usercenter.service.CacheService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.function.Supplier;

/**
 * @author lipeng
 * @description
 * @since 2025/6/24 8:26
 */
@Service
@Slf4j
public class CacheServiceImpl implements CacheService {

    private final RedissonClient redissonClient;

    public CacheServiceImpl(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    @Override
    public <T> T getWithCache(String cacheKey, Supplier<T> supplier, Duration expireDuration) {
        try {
            T cachedData = getFromCache(cacheKey);
            if (cachedData != null) {
                log.info("Cache hit for key: {}", cacheKey);
                return cachedData;
            }

            log.info("Cache miss for key: {}", cacheKey);
            T data = supplier.get();

            if (data != null) {
                asyncUpdateCache(cacheKey, data, expireDuration);
            }

            return data;
        } catch (Exception e) {
            log.error("Cache operation failed for key: {}", cacheKey, e);
            return supplier.get();
        }
    }

    /**
     * 从缓存获取数据
     */
    private <T> T getFromCache(String cacheKey) {
        try {
            RBucket<T> bucket = redissonClient.getBucket(cacheKey);
            return bucket.get();
        } catch (Exception e) {
            log.warn("Failed to get data from cache: {}", cacheKey, e);
            return null;
        }
    }

    @Override
    @Async("cacheExecutor")
    public <T> void asyncUpdateCache(String cacheKey, T data, Duration expireDuration) {
        try {
            RBucket<T> bucket = redissonClient.getBucket(cacheKey);
            Duration finalExpiration = addRandomOffset(expireDuration);
            bucket.set(data, finalExpiration);
            log.debug("Cache updated for key: {}", cacheKey);
        } catch (Exception e) {
            log.error("Failed to update cache for key: {}", cacheKey, e);
        }
    }

    @Override
    public boolean deleteCache(String cacheKey) {
        RBucket<Object> rBucket = redissonClient.getBucket(cacheKey);
        return rBucket.delete();
    }

    /**
     * 添加随机偏移量防止缓存雪崩
     */
    private Duration addRandomOffset(Duration baseDuration) {
        long baseSeconds = baseDuration.getSeconds();
        // 添加 ±10% 的随机偏移
        long offsetSeconds = (long) (baseSeconds * 0.1 * (Math.random() * 2 - 1));
        return Duration.ofSeconds(baseSeconds + offsetSeconds);
    }

    /**
     * 删除缓存
     */
    public void evictCache(String cacheKey) {
        try {
            redissonClient.getBucket(cacheKey).delete();
            log.info("Cache evicted for key: {}", cacheKey);
        } catch (Exception e) {
            log.error("Failed to evict cache for key: {}", cacheKey, e);
        }
    }

}
