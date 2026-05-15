package com.inkwell.messaging.controller;

import com.inkwell.messaging.dto.CampaignRequest;
import com.inkwell.messaging.dto.SubscriberDTO;
import com.inkwell.messaging.dto.SubscriptionRequest;
import com.inkwell.messaging.dto.SubscriptionResponse;
import com.inkwell.messaging.service.NewsletterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Newsletter Resource Tests")
class NewsletterResourceTest {

    @Mock
    private NewsletterService newsletterService;

    private NewsletterResource resource;

    @BeforeEach
    void setUp() {
        resource = new NewsletterResource(newsletterService);
    }

    @Test
    @DisplayName("Should subscribe, confirm, and query subscribers")
    void newsletterEndpoints_delegateToService() {
        SubscriptionRequest request = SubscriptionRequest.builder().email("test@example.com").build();
        SubscriptionResponse response = SubscriptionResponse.builder()
                .subscriberId(1)
                .email("test@example.com")
                .status("ACTIVE")
                .build();
        SubscriberDTO subscriber = SubscriberDTO.builder().subscriberId(1).email("test@example.com").build();
        when(newsletterService.subscribe(request)).thenReturn(response);
        when(newsletterService.confirmSubscription("token")).thenReturn(response);
        when(newsletterService.approveSubscriber(1)).thenReturn(response);
        when(newsletterService.getSubscriberById(1)).thenReturn(subscriber);
        when(newsletterService.getSubscriberByEmail("test@example.com")).thenReturn(subscriber);
        when(newsletterService.getAllSubscribers()).thenReturn(List.of(subscriber));

        assertEquals(HttpStatus.CREATED, resource.subscribe(request).getStatusCode());
        assertEquals("ACTIVE", resource.confirmSubscription("token").getBody().getStatus());
        assertEquals("ACTIVE", resource.approveSubscriber(1).getBody().getStatus());
        assertEquals(1, resource.getSubscriberById(1).getBody().getSubscriberId());
        assertEquals("test@example.com", resource.getSubscriberByEmail("test@example.com").getBody().getEmail());
        assertEquals(1, resource.getAllSubscribers().getBody().size());
    }

    @Test
    @DisplayName("Should unsubscribe and send campaigns")
    void mutationEndpoints_delegateToService() {
        CampaignRequest campaign = CampaignRequest.builder().subject("Hi").body("Body").build();

        assertEquals(HttpStatus.OK, resource.unsubscribe("test@example.com", "token").getStatusCode());
        assertEquals(HttpStatus.OK, resource.sendCampaign(campaign).getStatusCode());

        verify(newsletterService).unsubscribe("test@example.com", "token");
        verify(newsletterService).sendCampaign(campaign);
    }
}
