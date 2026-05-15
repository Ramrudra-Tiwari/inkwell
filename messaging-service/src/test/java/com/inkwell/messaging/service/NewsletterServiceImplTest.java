package com.inkwell.messaging.service;

import com.inkwell.messaging.dto.SubscriptionRequest;
import com.inkwell.messaging.dto.SubscriptionResponse;
import com.inkwell.messaging.dto.CampaignRequest;
import com.inkwell.messaging.entity.Subscriber;
import com.inkwell.messaging.exception.SubscriberNotFoundException;
import com.inkwell.messaging.exception.SubscriptionException;
import com.inkwell.messaging.repository.SubscriberRepository;
import com.inkwell.messaging.service.impl.NewsletterServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NewsletterServiceImplTest {

    @Mock
    private SubscriberRepository subscriberRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private NewsletterServiceImpl newsletterService;

    private SubscriptionRequest subscriptionRequest;
    private Subscriber subscriber;

    @BeforeEach
    void setUp() {
        subscriptionRequest = SubscriptionRequest.builder()
                .email("test@example.com")
                .userId(1)
                .preferences("Technology, Programming")
                .build();

        subscriber = Subscriber.builder()
                .subscriberId(1)
                .email("test@example.com")
                .userId(1)
                .token("test-token")
                .preferences("Technology, Programming")
                .status(Subscriber.SubscriberStatus.PENDING)
                .build();
    }

    @Test
    @DisplayName("Should successfully subscribe new user")
    void testSubscribe_Success() {
        // Given
        when(subscriberRepository.existsByEmailAndActiveOrPending(anyString())).thenReturn(false);
        when(subscriberRepository.save(any(Subscriber.class))).thenReturn(subscriber);

        // When
        SubscriptionResponse response = newsletterService.subscribe(subscriptionRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        assertThat(response.getStatus()).isEqualTo("PENDING");
        assertThat(response.getStatusCode()).isEqualTo(201);

        verify(subscriberRepository).existsByEmailAndActiveOrPending("test@example.com");
        verify(subscriberRepository).save(any(Subscriber.class));
        verify(emailService).sendConfirmationEmail(anyString(), anyString());
    }

    @Test
    @DisplayName("Should throw exception when email already subscribed")
    void testSubscribe_AlreadySubscribed() {
        // Given
        when(subscriberRepository.existsByEmailAndActiveOrPending(anyString())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> newsletterService.subscribe(subscriptionRequest))
                .isInstanceOf(SubscriptionException.class)
                .hasMessage("Email already subscribed: test@example.com");

        verify(subscriberRepository).existsByEmailAndActiveOrPending("test@example.com");
        verify(subscriberRepository, never()).save(any(Subscriber.class));
        verify(emailService, never()).sendConfirmationEmail(anyString(), anyString());
    }

    @Test
    @DisplayName("Should confirm subscription successfully")
    void testConfirmSubscription_Success() {
        // Given
        when(subscriberRepository.findByToken("test-token")).thenReturn(Optional.of(subscriber));
        when(subscriberRepository.updateStatusByToken(anyString(), any())).thenReturn(1);

        // When
        SubscriptionResponse response = newsletterService.confirmSubscription("test-token");

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        assertThat(response.getStatus()).isEqualTo("ACTIVE");
        assertThat(response.getStatusCode()).isEqualTo(200);

        verify(subscriberRepository).findByToken("test-token");
        verify(subscriberRepository).updateStatusByToken("test-token", Subscriber.SubscriberStatus.ACTIVE);
        verify(emailService).sendWelcomeEmail("test@example.com");
    }

    @Test
    @DisplayName("Should approve pending subscriber by id")
    void approveSubscriber_activatesPendingSubscriber() {
        when(subscriberRepository.findById(1)).thenReturn(Optional.of(subscriber));

        SubscriptionResponse response = newsletterService.approveSubscriber(1);

        assertThat(response.getStatus()).isEqualTo("ACTIVE");
        assertThat(subscriber.getStatus()).isEqualTo(Subscriber.SubscriberStatus.ACTIVE);
        verify(subscriberRepository).save(subscriber);
        verify(emailService).sendWelcomeEmail("test@example.com");
    }

    @Test
    @DisplayName("Should throw exception for invalid token")
    void testConfirmSubscription_InvalidToken() {
        // Given
        when(subscriberRepository.findByToken("invalid-token")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> newsletterService.confirmSubscription("invalid-token"))
                .isInstanceOf(SubscriptionException.class)
                .hasMessage("Invalid or expired subscription token");

        verify(subscriberRepository).findByToken("invalid-token");
        verify(subscriberRepository, never()).updateStatusByToken(anyString(), any());
        verify(emailService, never()).sendWelcomeEmail(anyString());
    }

    @Test
    @DisplayName("Should unsubscribe when email and token match")
    void unsubscribe_updatesSubscriberStatus() {
        when(subscriberRepository.findByEmail("test@example.com")).thenReturn(Optional.of(subscriber));

        newsletterService.unsubscribe("test@example.com", "test-token");

        assertThat(subscriber.getStatus()).isEqualTo(Subscriber.SubscriberStatus.UNSUBSCRIBED);
        verify(subscriberRepository).save(subscriber);
    }

    @Test
    @DisplayName("Should reject unsubscribe with invalid token")
    void unsubscribe_throwsForInvalidToken() {
        when(subscriberRepository.findByEmail("test@example.com")).thenReturn(Optional.of(subscriber));

        assertThatThrownBy(() -> newsletterService.unsubscribe("test@example.com", "wrong-token"))
                .isInstanceOf(SubscriptionException.class);
        verify(subscriberRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should retrieve subscriber by id and email")
    void getSubscriber_returnsMappedSubscriber() {
        when(subscriberRepository.findById(1)).thenReturn(Optional.of(subscriber));
        when(subscriberRepository.findByEmail("test@example.com")).thenReturn(Optional.of(subscriber));

        assertThat(newsletterService.getSubscriberById(1).getEmail()).isEqualTo("test@example.com");
        assertThat(newsletterService.getSubscriberByEmail("test@example.com").getSubscriberId()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should throw when subscriber is missing")
    void getSubscriber_throwsWhenMissing() {
        when(subscriberRepository.findById(404)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> newsletterService.getSubscriberById(404))
                .isInstanceOf(SubscriberNotFoundException.class);
    }

    @Test
    @DisplayName("Should list all subscribers")
    void getAllSubscribers_returnsMappedList() {
        when(subscriberRepository.findAll()).thenReturn(List.of(subscriber));

        assertThat(newsletterService.getAllSubscribers()).hasSize(1);
    }

    @Test
    @DisplayName("Should send newsletter to active subscribers")
    void sendNewsletterToSubscribers_sendsEmail() {
        when(subscriberRepository.findAllActiveSubscribers()).thenReturn(List.of(subscriber));

        newsletterService.sendNewsletterToSubscribers("Title", "Summary", 5);

        verify(emailService).sendNewsletterEmail("test@example.com", "Title", "Summary", 5, "test-token");
    }

    @Test
    @DisplayName("Should send campaign to matching active subscribers")
    void sendCampaign_filtersAndSendsEmail() {
        subscriber.setStatus(Subscriber.SubscriberStatus.ACTIVE);
        CampaignRequest request = CampaignRequest.builder()
                .subject("Weekly")
                .body("Hello readers")
                .targetPreferences("tech")
                .build();
        when(subscriberRepository.findAll()).thenReturn(List.of(subscriber));

        newsletterService.sendCampaign(request);

        verify(emailService).sendCampaignEmail("test@example.com", "Weekly", "Hello readers", "test-token");
    }

    @Test
    @DisplayName("Should skip campaign subscribers when status does not match")
    void sendCampaign_skipsNonMatchingStatus() {
        subscriber.setStatus(Subscriber.SubscriberStatus.PENDING);
        CampaignRequest request = CampaignRequest.builder()
                .subject("Weekly")
                .body("Hello readers")
                .targetStatus("ACTIVE")
                .build();
        when(subscriberRepository.findAll()).thenReturn(List.of(subscriber));

        newsletterService.sendCampaign(request);

        verify(emailService, never()).sendCampaignEmail(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Should skip campaign subscribers when preferences do not match")
    void sendCampaign_skipsNonMatchingPreferences() {
        subscriber.setStatus(Subscriber.SubscriberStatus.ACTIVE);
        subscriber.setPreferences("Lifestyle");
        CampaignRequest request = CampaignRequest.builder()
                .subject("Weekly")
                .body("Hello readers")
                .targetPreferences("tech")
                .build();
        when(subscriberRepository.findAll()).thenReturn(List.of(subscriber));

        newsletterService.sendCampaign(request);

        verify(emailService, never()).sendCampaignEmail(anyString(), anyString(), anyString(), anyString());
    }
}
