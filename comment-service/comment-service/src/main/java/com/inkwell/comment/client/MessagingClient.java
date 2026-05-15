package com.inkwell.comment.client;

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
public class MessagingClient {

    private final RestTemplate restTemplate;
    private static final String MESSAGING_SERVICE_URL = "http://messaging-service/api/v1/notifications";

    public void sendNotification(Integer recipientId, Integer actorId, String type, String title, String message) {
        try {
            log.debug("Sending notification to recipient: {} of type: {}", recipientId, type);
            NotificationRequest request = NotificationRequest.builder()
                    .recipientId(recipientId)
                    .actorId(actorId)
                    .type(type)
                    .title(title)
                    .message(message)
                    .build();
            
            restTemplate.postForObject(MESSAGING_SERVICE_URL, request, Object.class);
        } catch (Exception e) {
            log.error("Failed to send notification to messaging-service for recipient: {}", recipientId, e);
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NotificationRequest {
        private Integer recipientId;
        private Integer actorId;
        private String type;
        private String title;
        private String message;
    }
}
