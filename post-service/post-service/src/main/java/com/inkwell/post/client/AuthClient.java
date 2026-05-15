package com.inkwell.post.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthClient {

    private final RestTemplate restTemplate;
    private static final String AUTH_SERVICE_URL = "http://auth-service/auth";

    public List<Integer> getFollowerIds(Integer userId) {
        try {
            log.debug("Fetching followers for userId: {}", userId);
            List<Integer> followerIds = restTemplate.getForObject(AUTH_SERVICE_URL + "/user/" + userId + "/followers", List.class);
            return followerIds;
        } catch (Exception e) {
            log.error("Failed to fetch followers from auth-service for userId: {}", userId, e);
        }
        return List.of();
    }
}
