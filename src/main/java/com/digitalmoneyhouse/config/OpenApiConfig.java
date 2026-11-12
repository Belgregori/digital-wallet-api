package com.digitalmoneyhouse.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI digitalMoneyHouseOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Digital Money House")
                        .description("API de la billetera virtual Digital Money House")
                        .version("1.0.0"));
    }
}
