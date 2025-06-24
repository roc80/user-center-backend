package com.yupi.usercenter.service;

import com.yupi.usercenter.constant.RedisConstant;
import org.springframework.stereotype.Component;

/**
 * @author lipeng
 * @description
 * @since 2025/6/24 8:33
 */
@Component
public class CacheKeyBuilder {
    public String buildUserSearchKey(int pageNum, int pageSize) {
        return String.format("%s:%s:searchAllUsers:pageNum=%d,pageSize=%d",
                RedisConstant.PROJECT_NAME,
                RedisConstant.MODULE_CACHE,
                pageNum,
                pageSize
        );
    }

    public String buildUserRecommendKey(Long userId, int pageNum, int pageSize) {
        return String.format("%s:%s:recommendUsers:userId=%d:pageNum=%d,pageSize=%d",
                RedisConstant.PROJECT_NAME,
                RedisConstant.MODULE_CACHE,
                userId,
                pageNum,
                pageSize
        );
    }
}
