package com.example.DocumentService.Document.Client;

import com.example.DocumentService.Document.ApiResponse;
import com.example.DocumentService.Document.Exceptionhandler.GlobalExceptionHandler.*;
import com.example.DocumentService.UserDTO.UserInfoDTO;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import org.springframework.http.HttpHeaders;
import java.time.Duration;

@Slf4j
@Service
public class UserServiceClient {

    private final WebClient userWebClient;
    private final UserServiceProperties properties;

    public UserServiceClient(@Qualifier("userWebClient") WebClient userWebClient,
                             UserServiceProperties properties) {
        this.userWebClient = userWebClient;
        this.properties = properties;
    }


    @CircuitBreaker(name = "userService", fallbackMethod = "fallbackGetUserInfo")
    @Bulkhead(name = "userService")
    public Mono<UserInfoDTO> getUserInfo(String authToken) {
        return userWebClient.get()
                .uri(properties.getUserEndpoint())
                .header(HttpHeaders.AUTHORIZATION, authToken)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResponse<UserInfoDTO>>() {})
                .map(ApiResponse::getData)
                .timeout(Duration.ofMillis(properties.getReadTimeout()))
                .doOnSuccess(userInfo -> log.debug("Successfully fetched user info for user: {}",
                        userInfo != null ? userInfo.getUserId() : "null"))
                .doOnError(error -> log.error("Failed to fetch user info", error))
                .onErrorMap(Exception.class, exception -> {
                    return exception;
                });
    }

    public Mono<UserInfoDTO> fallbackGetUserInfo(String authToken, Exception exception) {
        log.error("Fallback method called for getUserInfo with token: {}, due to: {}",
                authToken != null ? "***PRESENT***" : "NULL", exception.getMessage());
        return Mono.error(new ServiceUnavailableException(
                "User service is currently unavailable. Please try again later. Error: " + exception.getMessage()));
    }
}