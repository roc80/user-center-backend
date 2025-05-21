package com.yupi.usercenter.tasks;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yupi.usercenter.constant.RedisConstant;
import com.yupi.usercenter.model.User;
import com.yupi.usercenter.service.UserService;
import com.yupi.usercenter.utils.aspect.RedissonTryLock;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.Duration;

@Component
@Slf4j
public class ScheduledTask {
    @Autowired
    RedissonClient redissonClient;
    @Resource
    UserService userService;

    @Scheduled(cron = "0 0 5 * * ?")
    @RedissonTryLock
    public void doPreCache() {
        log.info("prepare cache");
        // TODO@lp 哪些用户使用缓存
        for (long userId = 1; userId < 100; userId++) {
            User user = userService.getById(userId);
            if (user != null) {
                String redisKeyName = String.format(RedisConstant.PROJECT_NAME + ":" + RedisConstant.MODULE_RECOMMEND + ":recommendUsers:%d", user.getId());
                RBucket<Page<User>> rBucket = redissonClient.getBucket(redisKeyName);
                Page<User> resultPage = userService.page(new Page<>(0, 20));
                // 写缓存 TODO@lp 过期时间加offset,防止缓存雪崩。
                rBucket.set(resultPage, Duration.ofHours(8));
            }
        }
    }
}
