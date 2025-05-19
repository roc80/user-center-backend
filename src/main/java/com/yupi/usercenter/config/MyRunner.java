package com.yupi.usercenter.config;

import com.yupi.usercenter.once.ImportUsers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MyRunner implements ApplicationRunner {

    @Autowired
    ImportUsers importUsers;

    @Override
    public void run(ApplicationArguments args) throws Exception {
//        importUsers.addFakeUsers3();

    }
}
