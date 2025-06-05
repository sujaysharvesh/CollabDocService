package com.example.DocumentService.Document.Client;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "app.user-service")
@Data
@Component
public class UserServiceProperties {
    private String baseUrl = "http://user-service";
    private int timeout = 5000;
    private int maxRetries = 3;
    private int readTimeout = 5000;
    private int retryDelay = 1000;
}
