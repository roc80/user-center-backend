package com.yupi.usercenter.once;

import com.yupi.usercenter.model.User;
import com.yupi.usercenter.service.UserService;
import com.yupi.usercenter.utils.aspect.CalcExecutionTime;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Component
public class ImportUsers {

    int INSERT_NUM = 100_000;

    @Resource
    UserService userService;

    /**
     * spent time 286s
     * @author lipeng
     * @since 2025/5/19 13:55
    */
    @CalcExecutionTime
    public void addFakeUsers1() {
        for (int i = 0; i < INSERT_NUM; i++) {
            userService.save(getUser());
        }
    }

    /**
     * spent time 43s
     * @author lipeng
     * @since 2025/5/19 17:15
    */
    @CalcExecutionTime
    public void addFakeUsers2() {
        int batchSize = 125;
        int loopCount = INSERT_NUM / batchSize;
        ArrayList<User> users = new ArrayList<>();
        for (int i = 0; i < loopCount; i++) {
            for (int j = 0; j < batchSize; j++) {
                users.add(getUser());
            }
            userService.saveBatch(users, batchSize);
            users.clear();
        }
    }

    @NotNull
    private static User getUser() {
        User user = new User();
        user.setUserName("fake_roc");
        user.setAvatarUrl("https://avatars.githubusercontent.com/u/85272827?v=4");
        user.setUserPassword("12345678");
        user.setGender(0);
        user.setPhone("111");
        user.setEmail("222");
        user.setCreateDatetime(new Date());
        user.setUpdateDatetime(new Date());
        user.setValid(0);
        user.setDelete(0);
        user.setUserRole(0);
        user.setTagJsonList("");
        return user;
    }

    /**
     * spent time 10.45s
     * @author lipeng
     * @since 2025/5/19 21:58
    */
    @CalcExecutionTime
    public void addFakeUsers3() {
        // 多线程 批量插入
        int cpuCores = Runtime.getRuntime().availableProcessors();
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                cpuCores,
                cpuCores * 2,
                2000,
                TimeUnit.MICROSECONDS,
                new ArrayBlockingQueue<>(400),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
        int batchSize = 125;
        int taskNum = 16;
        CountDownLatch latch = new CountDownLatch(taskNum);
        for (int i = 0; i < taskNum; i++) {
            executor.submit(() -> {
                System.out.println(Thread.currentThread().getName() + "=========start work");
                ArrayList<User> users = new ArrayList<>();
                int num = INSERT_NUM / taskNum;
                int loopNum = num / batchSize;
                for (int j = 0; j < loopNum; j++) {
                    for (int k = 0; k < batchSize; k++) {
                        users.add(getUser());
                    }
                    userService.saveBatch(users, batchSize);
                    users.clear();
                }
                System.out.println(Thread.currentThread().getName() + "====will finished");
                latch.countDown();
            });
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        executor.shutdown();
        System.out.println("所有线程已经执行完毕");
    }

}
