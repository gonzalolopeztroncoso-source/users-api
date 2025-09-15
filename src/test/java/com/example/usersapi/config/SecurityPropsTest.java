package com.example.usersapi.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

class SecurityPropsTest {

    private final ApplicationContextRunner ctxRunner =
            new ApplicationContextRunner().withUserConfiguration(TestConfig.class);

    @Configuration
    @EnableConfigurationProperties(SecurityProps.class)
    static class TestConfig {}

    @Test
    void binds_all_properties() {
        ctxRunner
                .withPropertyValues(
                        "security.jwt.secret=s3cr3t",
                        "security.jwt.expiration-minutes=42")
                .run(ctx -> {
                    var p = ctx.getBean(SecurityProps.class);
                    assertThat(p.getSecret()).isEqualTo("s3cr3t");
                    assertThat(p.getExpirationMinutes()).isEqualTo(42L);
                });
    }

    @Test
    void uses_default_expiration_when_missing() {
        ctxRunner
                .withPropertyValues("security.jwt.secret=abc")
                .run(ctx -> {
                    var p = ctx.getBean(SecurityProps.class);
                    assertThat(p.getSecret()).isEqualTo("abc");
                    assertThat(p.getExpirationMinutes()).isEqualTo(120L);
                });
    }
}
