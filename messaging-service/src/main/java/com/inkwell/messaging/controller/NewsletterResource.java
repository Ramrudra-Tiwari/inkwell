package com.inkwell.messaging.controller;

import com.inkwell.messaging.dto.SubscriberDTO;
import com.inkwell.messaging.dto.SubscriptionRequest;
import com.inkwell.messaging.dto.SubscriptionResponse;
import com.inkwell.messaging.service.NewsletterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for newsletter subscription operations.
 */
@RestController
@RequestMapping("/api/v1/newsletter")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Newsletter Service", description = "APIs for managing newsletter subscriptions")
public class NewsletterResource {

    private final NewsletterService newsletterService;

    @PostMapping("/subscribe")
    @Operation(summary = "Subscribe to newsletter", description = "Subscribe to newsletter with double opt-in flow")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Subscription created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request or email already subscribed")
    })
    public ResponseEntity<SubscriptionResponse> subscribe(@Valid @RequestBody SubscriptionRequest request) {
        log.info("Newsletter subscription request for email: {}", request.getEmail());

        SubscriptionResponse response = newsletterService.subscribe(request);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/confirm")
    @Operation(summary = "Confirm newsletter subscription", description = "Confirm subscription using the token sent via email")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Subscription confirmed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid or expired token")
    })
    public ResponseEntity<SubscriptionResponse> confirmSubscription(
            @Parameter(description = "Confirmation token sent via email")
            @RequestParam String token) {

        log.info("Newsletter confirmation request with token");

        SubscriptionResponse response = newsletterService.confirmSubscription(token);

        return ResponseEntity.ok(response);
    }

    @PostMapping({"/subscriber/{subscriberId}/approve", "/subscribers/{subscriberId}/approve"})
    @Operation(summary = "Approve pending subscriber", description = "Approve a pending newsletter subscriber directly from the admin console")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Subscriber approved successfully"),
        @ApiResponse(responseCode = "404", description = "Subscriber not found")
    })
    public ResponseEntity<SubscriptionResponse> approveSubscriber(
            @Parameter(description = "Subscriber ID")
            @PathVariable Integer subscriberId) {

        log.info("Newsletter subscriber approval request for id: {}", subscriberId);

        SubscriptionResponse response = newsletterService.approveSubscriber(subscriberId);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/unsubscribe")
    @Operation(summary = "Unsubscribe from newsletter", description = "Unsubscribe from newsletter using email and secure token")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Unsubscribed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid token"),
        @ApiResponse(responseCode = "404", description = "Subscriber not found")
    })
    public ResponseEntity<Void> unsubscribe(
            @Parameter(description = "Email address to unsubscribe")
            @RequestParam String email,
            @Parameter(description = "Security token for unsubscription")
            @RequestParam String token) {

        log.info("Newsletter unsubscribe request for email: {} with token", email);

        newsletterService.unsubscribe(email, token);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/subscriber/{subscriberId}")
    @Operation(summary = "Get subscriber by ID", description = "Retrieve subscriber information by subscriber ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Subscriber found"),
        @ApiResponse(responseCode = "404", description = "Subscriber not found")
    })
    public ResponseEntity<SubscriberDTO> getSubscriberById(
            @Parameter(description = "Subscriber ID")
            @PathVariable Integer subscriberId) {

        log.debug("Getting subscriber by ID: {}", subscriberId);

        SubscriberDTO subscriber = newsletterService.getSubscriberById(subscriberId);

        return ResponseEntity.ok(subscriber);
    }

    @GetMapping("/subscriber")
    @Operation(summary = "Get subscriber by email", description = "Retrieve subscriber information by email")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Subscriber found"),
        @ApiResponse(responseCode = "404", description = "Subscriber not found")
    })
    public ResponseEntity<SubscriberDTO> getSubscriberByEmail(
            @Parameter(description = "Email address")
            @RequestParam String email) {

        log.debug("Getting subscriber by email: {}", email);

        SubscriberDTO subscriber = newsletterService.getSubscriberByEmail(email);

        return ResponseEntity.ok(subscriber);
    }

    @GetMapping("/subscribers/all")
    @Operation(summary = "Get all subscribers", description = "Retrieve a list of all newsletter subscribers (Admin only)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Subscribers retrieved successfully")
    })
    public ResponseEntity<java.util.List<SubscriberDTO>> getAllSubscribers() {
        log.info("Request to get all newsletter subscribers");
        return ResponseEntity.ok(newsletterService.getAllSubscribers());
    }

    @PostMapping("/campaign")
    @Operation(summary = "Send newsletter campaign", description = "Broadcast a manual newsletter campaign to all active subscribers")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Campaign broadcasted successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid campaign content")
    })
    public ResponseEntity<Void> sendCampaign(@Valid @RequestBody com.inkwell.messaging.dto.CampaignRequest request) {
        log.info("Newsletter campaign broadcast initiated: {}", request.getSubject());

        newsletterService.sendCampaign(request);

        return ResponseEntity.ok().build();
    }
}
