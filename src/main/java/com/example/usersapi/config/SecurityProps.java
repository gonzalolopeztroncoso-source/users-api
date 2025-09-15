package com.example.usersapi.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "security.jwt")
public class SecurityProps {
    private String secret;
    private long expirationMinutes = 120;
    public String getSecret() { return secret; }
    public void setSecret(String secret) { this.secret = secret; }
    public long getExpirationMinutes() { return expirationMinutes; }
    public void setExpirationMinutes(long m) { this.expirationMinutes = m; }
}
