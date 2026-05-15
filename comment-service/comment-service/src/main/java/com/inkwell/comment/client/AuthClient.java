package com.inkwell.comment.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthClient {

    private final RestTemplate restTemplate;
    private static final String AUTH_SERVICE_URL = "http://auth-service/auth";

    public Integer getUserIdByUsername(String username) {
        try {
            log.debug("Fetching userId for username: {}", username);
            Map<String, Object> response = restTemplate.getForObject(AUTH_SERVICE_URL + "/username/" + username, Map.class);
            if (response != null && response.containsKey("userId")) {
                return (Integer) response.get("userId");
            }
        } catch (Exception e) {
            log.error("Failed to fetch user from auth-service for username: {}", username, e);
        }
        return null;
    }
}
