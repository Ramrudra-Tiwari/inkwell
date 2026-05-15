package com.inkwell.post.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
@Slf4j
public class CommentClient {

    private final RestTemplate restTemplate;
    private final String COMMENT_SERVICE_URL = "http://comment-service/api/v1/comments";

    public void deleteCommentsByPostId(Integer postId) {
        try {
            log.info("Requesting deletion of all comments for post ID: {}", postId);
            restTemplate.delete(COMMENT_SERVICE_URL + "/post/" + postId);
        } catch (Exception e) {
            log.error("Failed to delete comments for post ID: {}", postId, e);
        }
    }

    public Integer getCommentCountByPostId(Integer postId) {
        try {
            log.debug("Fetching comment count for post ID: {}", postId);
            return restTemplate.getForObject(COMMENT_SERVICE_URL + "/post/" + postId + "/count", Integer.class);
        } catch (Exception e) {
            log.error("Failed to fetch comment count for post ID: {}", postId, e);
            return 0;
        }
    }
}
