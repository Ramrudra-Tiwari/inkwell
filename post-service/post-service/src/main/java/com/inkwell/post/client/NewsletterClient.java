package com.inkwell.post.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
@RequiredArgsConstructor
public class NewsletterClient {

    private final RestTemplate restTemplate;
    private static final String MESSAGING_SERVICE_URL = "http://messaging-service/newsletter";

    public void triggerNewsletterForNewPost(String title, String excerpt, Integer postId) {
        try {
            log.info("Triggering newsletter distribution for new post: {}", title);
            restTemplate.postForLocation(MESSAGING_SERVICE_URL + "/trigger-new-post", new NewPostNewsletterRequest(title, excerpt, postId));
        } catch (Exception e) {
            log.error("Failed to trigger newsletter for post: {}", title, e);
        }
    }

    private record NewPostNewsletterRequest(String postTitle, String postSummary, Integer postId) {}
}
