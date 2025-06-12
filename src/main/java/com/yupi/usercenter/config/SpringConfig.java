package com.yupi.usercenter.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableAspectJAutoProxy
@EnableScheduling
@EnableConfigurationProperties(MyConfigProperty.class)
public class SpringConfig {

}
