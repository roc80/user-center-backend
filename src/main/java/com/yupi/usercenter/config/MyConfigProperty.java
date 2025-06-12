package com.yupi.usercenter.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author lipeng
 * @description
 * @since 2025/6/12 13:03
 */
@ConfigurationProperties(prefix = "my-config")
@Data
public class MyConfigProperty {

    private Security security;

    @Data
    public static class Security {
        private String salt;
    }

}
