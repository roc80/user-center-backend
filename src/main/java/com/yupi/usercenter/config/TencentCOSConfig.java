package com.yupi.usercenter.config;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.region.Region;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author lipeng
 * @description
 * @since 2025/6/24 14:44
 */
@Configuration
@ConfigurationProperties(prefix = "tencent.cos")
@Data
public class TencentCOSConfig {
    private String secretId;
    private String secretKey;
    private String region;
    private String bucketName;
    private String baseUrl;

    @Bean
    public COSClient cosClient() {
        // 1 初始化用户身份信息（secretId, secretKey）
        COSCredentials cred = new BasicCOSCredentials(secretId, secretKey);

        // 2 设置 bucket 的地域
        Region regionObj = new Region(region);
        ClientConfig clientConfig = new ClientConfig(regionObj);

        // 3 生成 cos 客户端
        return new COSClient(cred, clientConfig);
    }
}
