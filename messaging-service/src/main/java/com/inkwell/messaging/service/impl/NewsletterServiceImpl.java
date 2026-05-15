package com.inkwell.messaging.service.impl;

import com.inkwell.messaging.dto.SubscriberDTO;
import com.inkwell.messaging.dto.SubscriptionRequest;
import com.inkwell.messaging.dto.SubscriptionResponse;
import com.inkwell.messaging.entity.Subscriber;
import com.inkwell.messaging.exception.SubscriberNotFoundException;
import com.inkwell.messaging.exception.SubscriptionException;
import com.inkwell.messaging.repository.SubscriberRepository;
import com.inkwell.messaging.service.EmailService;
import com.inkwell.messaging.service.NewsletterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Implementation of NewsletterService for managing newsletter subscriptions.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NewsletterServiceImpl implements NewsletterService {

    private final SubscriberRepository subscriberRepository;
    private final EmailService emailService;

    @Override
    public SubscriptionResponse subscribe(SubscriptionRequest request) {
        log.info("Processing newsletter subscription for email: {}", request.getEmail());

        // Check if email is already subscribed
        if (subscriberRepository.existsByEmailAndActiveOrPending(request.getEmail())) {
            throw SubscriptionException.alreadySubscribed(request.getEmail());
        }

        // Generate unique token
        String token = UUID.randomUUID().toString();

        // Create subscriber with PENDING status
        Subscriber subscriber = Subscriber.builder()
                .email(request.getEmail())
                .userId(request.getUserId())
                .token(token)
                .preferences(request.getPreferences())
                .status(Subscriber.SubscriberStatus.PENDING)
                .build();

        subscriber = subscriberRepository.save(subscriber);

        // Send confirmation email
        emailService.sendConfirmationEmail(subscriber.getEmail(), token);

        log.info("Newsletter subscription created for email: {} with status PENDING", request.getEmail());

        return SubscriptionResponse.builder()
                .subscriberId(subscriber.getSubscriberId())
                .email(subscriber.getEmail())
                .status(subscriber.getStatus().name())
                .message("Subscription created. Please check your email to confirm.")
                .statusCode(201)
                .build();
    }

    @Override
    public SubscriptionResponse confirmSubscription(String token) {
        log.info("Confirming newsletter subscription with token: {}", token);

        Subscriber subscriber = subscriberRepository.findByToken(token)
                .orElseThrow(() -> SubscriptionException.invalidToken());

        if (subscriber.getStatus() == Subscriber.SubscriberStatus.ACTIVE) {
            throw SubscriptionException.alreadyActivated();
        }

        // Update status to ACTIVE
        int updatedRows = subscriberRepository.updateStatusByToken(token, Subscriber.SubscriberStatus.ACTIVE);
        if (updatedRows == 0) {
            throw SubscriptionException.invalidToken();
        }

        // Send welcome email
        emailService.sendWelcomeEmail(subscriber.getEmail());

        log.info("Newsletter subscription confirmed for email: {}", subscriber.getEmail());

        return SubscriptionResponse.builder()
                .subscriberId(subscriber.getSubscriberId())
                .email(subscriber.getEmail())
                .status(Subscriber.SubscriberStatus.ACTIVE.name())
                .message("Subscription confirmed successfully. Welcome to our newsletter!")
                .statusCode(200)
                .build();
    }

    @Override
    public SubscriptionResponse approveSubscriber(Integer subscriberId) {
        log.info("Approving newsletter subscriber with id: {}", subscriberId);

        Subscriber subscriber = subscriberRepository.findById(subscriberId)
                .orElseThrow(() -> SubscriberNotFoundException.forId(subscriberId));

        if (subscriber.getStatus() == Subscriber.SubscriberStatus.ACTIVE) {
            return SubscriptionResponse.builder()
                    .subscriberId(subscriber.getSubscriberId())
                    .email(subscriber.getEmail())
                    .status(Subscriber.SubscriberStatus.ACTIVE.name())
                    .message("Subscriber is already active.")
                    .statusCode(200)
                    .build();
        }

        subscriber.setStatus(Subscriber.SubscriberStatus.ACTIVE);
        subscriberRepository.save(subscriber);
        emailService.sendWelcomeEmail(subscriber.getEmail());

        log.info("Newsletter subscriber approved for email: {}", subscriber.getEmail());

        return SubscriptionResponse.builder()
                .subscriberId(subscriber.getSubscriberId())
                .email(subscriber.getEmail())
                .status(Subscriber.SubscriberStatus.ACTIVE.name())
                .message("Subscriber approved successfully.")
                .statusCode(200)
                .build();
    }

    @Override
    public void unsubscribe(String email, String token) {
        log.info("Unsubscribing email: {} with token: {}", email, token);

        Subscriber subscriber = subscriberRepository.findByEmail(email)
                .orElseThrow(() -> SubscriberNotFoundException.forEmail(email));
        
        if (!subscriber.getToken().equals(token)) {
            log.warn("Unsubscribe attempt failed: Invalid token for email {}", email);
            throw SubscriptionException.invalidToken();
        }

        subscriber.setStatus(Subscriber.SubscriberStatus.UNSUBSCRIBED);
        subscriberRepository.save(subscriber);

        log.info("Successfully unsubscribed email: {}", email);
    }

    @Override
    @Transactional(readOnly = true)
    public SubscriberDTO getSubscriberById(Integer subscriberId) {
        log.debug("Getting subscriber by ID: {}", subscriberId);

        Subscriber subscriber = subscriberRepository.findById(subscriberId)
                .orElseThrow(() -> SubscriberNotFoundException.forId(subscriberId));

        return mapToDTO(subscriber);
    }

    @Override
    @Transactional(readOnly = true)
    public SubscriberDTO getSubscriberByEmail(String email) {
        log.debug("Getting subscriber by email: {}", email);

        Subscriber subscriber = subscriberRepository.findByEmail(email)
                .orElseThrow(() -> SubscriberNotFoundException.forEmail(email));

        return mapToDTO(subscriber);
    }

    @Override
    public void sendNewsletterToSubscribers(String postTitle, String postSummary, Integer postId) {
        log.info("Sending newsletter for new post: {}", postTitle);

        List<Subscriber> activeSubscribers = subscriberRepository.findAllActiveSubscribers();

        log.info("Found {} active subscribers to send newsletter to", activeSubscribers.size());

        for (Subscriber subscriber : activeSubscribers) {
            try {
                emailService.sendNewsletterEmail(subscriber.getEmail(), postTitle, postSummary, postId, subscriber.getToken());
                log.debug("Newsletter sent to: {}", subscriber.getEmail());
            } catch (Exception e) {
                log.error("Failed to send newsletter to: {}", subscriber.getEmail(), e);
            }
        }

        log.info("Newsletter distribution completed for post: {}", postTitle);
    }

    @Override
    public void sendCampaign(com.inkwell.messaging.dto.CampaignRequest request) {
        log.info("Broadcasting newsletter campaign: {}", request.getSubject());

        List<Subscriber> subscribers = subscriberRepository.findAll();
        
        // Filter subscribers based on request
        List<Subscriber> targets = subscribers.stream()
                .filter(s -> {
                    // Filter by status if specified, otherwise only ACTIVE
                    if (request.getTargetStatus() != null && !request.getTargetStatus().isEmpty()) {
                        return s.getStatus().name().equalsIgnoreCase(request.getTargetStatus());
                    }
                    return s.getStatus() == Subscriber.SubscriberStatus.ACTIVE;
                })
                .filter(s -> {
                    // Filter by preferences if specified
                    if (request.getTargetPreferences() != null && !request.getTargetPreferences().isEmpty()) {
                        return s.getPreferences() != null && s.getPreferences().toLowerCase().contains(request.getTargetPreferences().toLowerCase());
                    }
                    return true;
                })
                .toList();

        log.info("Found {} target subscribers for campaign broadcast (Total active/matching filters)", targets.size());

        for (Subscriber subscriber : targets) {
            try {
                emailService.sendCampaignEmail(subscriber.getEmail(), request.getSubject(), request.getBody(), subscriber.getToken());
                log.debug("Campaign email sent to: {}", subscriber.getEmail());
            } catch (Exception e) {
                log.error("Failed to send campaign email to: {}", subscriber.getEmail(), e);
            }
        }

        log.info("Newsletter campaign broadcast completed: {}", request.getSubject());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubscriberDTO> getAllSubscribers() {
        log.debug("Fetching all newsletter subscribers");
        return subscriberRepository.findAll().stream()
                .map(this::mapToDTO)
                .toList();
    }

    private SubscriberDTO mapToDTO(Subscriber subscriber) {
        return SubscriberDTO.builder()
                .subscriberId(subscriber.getSubscriberId())
                .email(subscriber.getEmail())
                .userId(subscriber.getUserId())
                .status(subscriber.getStatus().name())
                .token(subscriber.getToken())
                .preferences(subscriber.getPreferences())
                .subscribedAt(subscriber.getSubscribedAt())
                .build();
    }
}
