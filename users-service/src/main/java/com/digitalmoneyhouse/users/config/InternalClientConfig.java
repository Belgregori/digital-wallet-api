package com.digitalmoneyhouse.users.config;

import com.digitalmoneyhouse.users.client.AccountClient;
import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(basePackageClasses = AccountClient.class)
public class InternalClientConfig {

    @Bean
    public RequestInterceptor accountInternalToken(@Value("${internal.api-key}") String apiKey) {
        return template -> {
            if (template.url().startsWith("/internal/")) {
                template.header("X-Internal-Token", apiKey);
            }
        };
    }
}
