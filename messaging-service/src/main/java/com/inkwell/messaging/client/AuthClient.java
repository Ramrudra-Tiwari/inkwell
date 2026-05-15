package com.inkwell.messaging.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthClient {

    private final RestTemplate restTemplate;
    private static final String AUTH_SERVICE_URL = "http://auth-service/user";

    public UserResponse getUser(Integer userId) {
        try {
            log.debug("Fetching user details for userId: {}", userId);
            return restTemplate.getForObject(AUTH_SERVICE_URL + "/" + userId, UserResponse.class);
        } catch (Exception e) {
            log.error("Failed to fetch user from auth-service for userId: {}", userId, e);
        }
        return null;
    }

    public UserResponse[] getAllUsers() {
        try {
            log.debug("Fetching all users from auth-service");
            // Note: AdminController maps /users to getAllUsers()
            return restTemplate.getForObject("http://auth-service/users", UserResponse[].class);
        } catch (Exception e) {
            log.error("Failed to fetch all users from auth-service", e);
        }
        return new UserResponse[0];
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserResponse {
        private Integer userId;
        private String username;
        private String email;
        private String fullName;
        private String role;
    }
}
