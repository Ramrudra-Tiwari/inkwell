package com.inkwell.messaging.listener;

import com.inkwell.messaging.config.RabbitMQConfig;
import com.inkwell.messaging.service.NewsletterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * RabbitMQ listener for newsletter-related messages.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NewsletterListener {

    private final NewsletterService newsletterService;

    /**
     * Listen for new post events and send newsletters to subscribers.
     */
    @RabbitListener(queues = RabbitMQConfig.NEWSLETTER_QUEUE)
    public void handleNewPostEvent(Map<String, Object> message) {
        try {
            log.info("Received new post event for newsletter distribution: {}", message);

            String postTitle = (String) message.get("title");
            String postSummary = (String) message.get("summary");
            Integer postId = (Integer) message.get("postId");

            if (postTitle == null || postSummary == null || postId == null) {
                log.error("Invalid message format. Missing required fields: title, summary, or postId");
                return;
            }

            newsletterService.sendNewsletterToSubscribers(postTitle, postSummary, postId);

            log.info("Successfully processed newsletter distribution for post: {}", postTitle);

        } catch (Exception e) {
            log.error("Error processing newsletter event: {}", e.getMessage(), e);
        }
    }
}
