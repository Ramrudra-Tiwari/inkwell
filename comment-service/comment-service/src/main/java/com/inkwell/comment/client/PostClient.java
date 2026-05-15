package com.inkwell.comment.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class PostClient {

    private final RestTemplate restTemplate;
    private static final String POST_SERVICE_URL = "http://post-service/api/v1/posts";

    public Integer getPostAuthorId(Integer postId) {
        try {
            log.debug("Fetching authorId for postId: {}", postId);
            Map<String, Object> response = restTemplate.getForObject(POST_SERVICE_URL + "/" + postId, Map.class);
            if (response != null && response.containsKey("authorId")) {
                return (Integer) response.get("authorId");
            }
        } catch (Exception e) {
            log.error("Failed to fetch post author from post-service for postId: {}", postId, e);
        }
        return null;
    }
}
