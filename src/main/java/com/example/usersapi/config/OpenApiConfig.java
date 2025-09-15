package com.example.usersapi.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public GroupedOpenApi usersApi() {
        return GroupedOpenApi.builder()
                .group("users")
                .packagesToScan("com.example.usersapi.controller")
                .pathsToMatch("/api/**")
                .build();
    }
}
