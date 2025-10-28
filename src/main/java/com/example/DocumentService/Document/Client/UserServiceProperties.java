package com.example.DocumentService.Document.Client;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;


@Data
@ConfigurationProperties(prefix = "app.user-service")
public class UserServiceProperties {
    private String baseUrl = "http://127.0.0.1:4005";
    private String userEndpoint = "/api/v1/auth/me";
    private int timeout = 500000;
    private int maxRetries = 3;
    private int readTimeout = 5000;
    private int retryDelay = 1000;
}
