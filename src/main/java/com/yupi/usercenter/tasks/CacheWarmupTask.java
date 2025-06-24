package com.yupi.usercenter.tasks;

import com.yupi.usercenter.constant.RedisConstant;
import com.yupi.usercenter.service.CacheKeyBuilder;
import com.yupi.usercenter.service.CacheService;
import com.yupi.usercenter.service.UserService;
import com.yupi.usercenter.service.UserTagService;
import com.yupi.usercenter.service.impl.UserServiceImpl;
import com.yupi.usercenter.utils.aspect.RedissonTryLock;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * @author lipeng
 * @description 缓存预热任务
 * @since 2025/6/24 10:47
 */
@Component
@Slf4j
public class CacheWarmupTask {

    private static final int[] PAGE_SIZES = {10, 20, 50};
    private static final int MAX_PAGES_TO_WARMUP = 5;

    private final UserService userService;
    private final RedissonClient redissonClient;
    private final CacheKeyBuilder cacheKeyBuilder;
    private final CacheService cacheService;
    private final UserTagService userTagService;

    public CacheWarmupTask(UserService userService, RedissonClient redissonClient, CacheKeyBuilder cacheKeyBuilder, CacheService cacheService, UserTagService userTagService) {
        this.userService = userService;
        this.redissonClient = redissonClient;
        this.cacheKeyBuilder = cacheKeyBuilder;
        this.cacheService = cacheService;
        this.userTagService = userTagService;
    }


    /**
     * 预热用户搜索缓存
     */
    @RedissonTryLock(
            key = RedisConstant.PROJECT_NAME + ":" + RedisConstant.MODULE_LOCK + ":" + "warmupUserSearchCache",
            waitTime = 0L, /*waitTime 指定为0，表示其他未能加锁成功的线程直接结束，在这里保证只有一个线程能获取到锁。*/
            leaseTime = -1L, /*leaseTime 指定为一个非正的长整型，启用WatchDog机制，自动续期。*/
            timeUnit = TimeUnit.MILLISECONDS
    )
    @Scheduled(cron = "0 0 2 * * ?")
    public void warmupUserSearchCache() {
        log.info("开始预热用户搜索缓存");
        CompletableFuture.runAsync(() -> {
            for (int pageSize : PAGE_SIZES) {
                for (int pageNum = 1; pageNum <= MAX_PAGES_TO_WARMUP; pageNum++) {
                    try {
                        String cacheKey = cacheKeyBuilder.buildUserSearchKey(pageNum, pageSize);
                        int finalPageNum = pageNum;
                        warmupCacheIfNotExists(cacheKey, () ->
                                ((UserServiceImpl) userService).getUsersFromDB(finalPageNum, pageSize)
                        );
                        Thread.sleep(100);
                    } catch (Exception e) {
                        log.error("预热用户搜索缓存失败 pageNum={}, pageSize={}", pageNum, pageSize, e);
                    }
                }
            }
            log.info("用户搜索缓存预热完成");
        });
    }

    private <T> void warmupCacheIfNotExists(String cacheKey, Supplier<T> dataSupplier) {
        try {
            RBucket<T> bucket = redissonClient.getBucket(cacheKey);
            if (!bucket.isExists()) {
                T data = dataSupplier.get();
                if (data != null) {
                    cacheService.asyncUpdateCache(cacheKey, data, CacheService.DEFAULT_CACHE_DURATION);
                    log.debug("缓存预热成功: {}", cacheKey);
                }
            }
        } catch (Exception e) {
            log.error("预热缓存失败: {}", cacheKey, e);
        }
    }

    @RedissonTryLock(
            key = RedisConstant.PROJECT_NAME + ":" + RedisConstant.MODULE_LOCK + ":" + "warmupUserRecommendCache",
            waitTime = 0L,
            leaseTime = -1L,
            timeUnit = TimeUnit.MILLISECONDS
    )
    @Scheduled(cron = "0 1 2 * * ?")
    public void warmupUserRecommendCache() {
        log.info("开始预热用户推荐缓存");
        CompletableFuture.runAsync(() -> {
            List<Long> allUserIdWithTag = userTagService.getAllUserWithTag();
            for (Long userId : allUserIdWithTag) {
                for (int pageSize : PAGE_SIZES) {
                    for (int pageNum = 1; pageNum <= MAX_PAGES_TO_WARMUP; pageNum++) {
                        try {
                            String cacheKey = cacheKeyBuilder.buildUserRecommendKey(userId, pageNum, pageSize);
                            int finalPageNum = pageNum;
                            warmupCacheIfNotExists(cacheKey, () ->
                                    ((UserServiceImpl) userService).recommendUsersFromDB(userService.getById(userId), finalPageNum, pageSize)
                            );
                            Thread.sleep(100);
                        } catch (Exception e) {
                            log.error("预热用户推荐缓存失败 pageNum={}, pageSize={}", pageNum, pageSize, e);
                        }
                    }
                }
            }
            log.info("用户推荐缓存预热完成");
        });
    }
}
