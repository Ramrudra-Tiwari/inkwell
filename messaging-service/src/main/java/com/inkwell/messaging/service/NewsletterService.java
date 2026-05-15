package com.inkwell.messaging.service;

import com.inkwell.messaging.dto.SubscriberDTO;
import com.inkwell.messaging.dto.SubscriptionRequest;
import com.inkwell.messaging.dto.SubscriptionResponse;

/**
 * Service interface for newsletter subscription operations.
 */
public interface NewsletterService {

    /**
     * Subscribe a user to the newsletter with double opt-in flow.
     */
    SubscriptionResponse subscribe(SubscriptionRequest request);

    /**
     * Confirm subscription using the token.
     */
    SubscriptionResponse confirmSubscription(String token);

    /**
     * Approve a pending subscriber directly by subscriber id.
     */
    SubscriptionResponse approveSubscriber(Integer subscriberId);

    /**
     * Unsubscribe from newsletter.
     */
    void unsubscribe(String email, String token);

    /**
     * Get subscriber by ID.
     */
    SubscriberDTO getSubscriberById(Integer subscriberId);

    /**
     * Get subscriber by email.
     */
    SubscriberDTO getSubscriberByEmail(String email);

    /**
     * Send newsletter to all active subscribers when a new post is published.
     */
    void sendNewsletterToSubscribers(String postTitle, String postSummary, Integer postId);

    /**
     * Send a manual newsletter campaign to all active subscribers.
     */
    void sendCampaign(com.inkwell.messaging.dto.CampaignRequest request);

    /**
     * Get all newsletter subscribers.
     */
    java.util.List<SubscriberDTO> getAllSubscribers();
}
