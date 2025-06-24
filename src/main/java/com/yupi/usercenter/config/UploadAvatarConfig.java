package com.yupi.usercenter.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

/**
 * @author lipeng
 * @description
 * @since 2025/6/24 14:54
 */
@Configuration
@ConfigurationProperties(prefix = "upload.avatar")
@Data
public class UploadAvatarConfig {
    private Long maxSize;
    private String allowedTypes;

    public List<String> getAllowedTypeList() {
        return Arrays.asList(allowedTypes.split(","));
    }
}
