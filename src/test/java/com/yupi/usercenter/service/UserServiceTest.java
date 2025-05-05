package com.yupi.usercenter.service;

import com.yupi.usercenter.model.User;
import com.yupi.usercenter.model.base.BaseResponse;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Set;

@SpringBootTest
class UserServiceTest {

    @Resource
    UserService userService;

    @Test
    void testAddUser() {

    }


    @Test
    void userRegister() {

    }

    @Test
    void userLogin() {

    }

    @Test
    void searchUsersByTags() {
        BaseResponse<Set<User>> response = userService.searchUsersByTags(Arrays.asList("Java", "男"));
        Assert.assertTrue(response.getData().isEmpty());
    }
}