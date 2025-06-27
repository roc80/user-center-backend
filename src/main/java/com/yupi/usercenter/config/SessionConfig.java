package com.yupi.usercenter.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;
import org.springframework.util.StringUtils;

/**
 * @author lipeng
 * @description
 * @since 2025/6/25 17:56
 */
@Configuration
public class SessionConfig{

    @Value("${app.cookie.domain:}")
    private String cookieDomain;

    @Bean
    public CookieSerializer cookieSerializer() {
        DefaultCookieSerializer serializer = new DefaultCookieSerializer();
        if (StringUtils.hasText(cookieDomain)) {
            serializer.setDomainName(cookieDomain);
            serializer.setCookiePath("/");
            serializer.setUseSecureCookie(true);
        }
        serializer.setUseHttpOnlyCookie(true);
        serializer.setSameSite("Lax");
        serializer.setCookieMaxAge(24 * 60 * 60);
        return serializer;
    }

}
