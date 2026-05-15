package com.inkwell.messaging.service;

import com.inkwell.messaging.client.AuthClient;
import com.inkwell.messaging.dto.NotificationDTO;
import com.inkwell.messaging.dto.NotificationRequest;
import com.inkwell.messaging.entity.Notification;
import com.inkwell.messaging.exception.NotificationNotFoundException;
import com.inkwell.messaging.repository.NotificationRepository;
import com.inkwell.messaging.service.impl.NotificationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Notification Service Tests")
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private AuthClient authClient;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private Notification notification;
    private NotificationRequest request;

    @BeforeEach
    void setUp() {
        notification = Notification.builder()
                .notificationId(1)
                .recipientId(15)
                .actorId(6)
                .type(Notification.NotificationType.NEW_COMMENT)
                .title("New comment")
                .message("Someone commented")
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();

        request = NotificationRequest.builder()
                .recipientId(15)
                .actorId(6)
                .type("NEW_COMMENT")
                .title("New comment")
                .message("Someone commented")
                .build();
    }

    @Test
    @DisplayName("Should create a notification")
    void createNotification_savesAndMapsNotification() {
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        NotificationDTO result = notificationService.createNotification(request);

        assertThat(result.getNotificationId()).isEqualTo(1);
        assertThat(result.getType()).isEqualTo("NEW_COMMENT");
        verify(emailService, never()).sendNotificationEmail(any(), any(), any());
    }

    @Test
    @DisplayName("Should send email for email-worthy notification")
    void createNotification_sendsEmailForNewPost() {
        request.setType("NEW_POST");
        notification.setType(Notification.NotificationType.NEW_POST);
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);
        when(authClient.getUser(15)).thenReturn(AuthClient.UserResponse.builder()
                .userId(15)
                .email("reader@example.com")
                .role("USER")
                .build());

        notificationService.createNotification(request);

        verify(emailService).sendNotificationEmail("reader@example.com", "New comment", "Someone commented");
    }

    @Test
    @DisplayName("Should not send email when auth user has no email")
    void createNotification_skipsEmailWhenUserHasNoEmail() {
        request.setType("COMMENT_REPLY");
        notification.setType(Notification.NotificationType.COMMENT_REPLY);
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);
        when(authClient.getUser(15)).thenReturn(AuthClient.UserResponse.builder()
                .userId(15)
                .role("USER")
                .build());

        notificationService.createNotification(request);

        verify(emailService, never()).sendNotificationEmail(any(), any(), any());
    }

    @Test
    @DisplayName("Should keep notification creation successful when email lookup fails")
    void createNotification_ignoresEmailFailure() {
        request.setType("COMMENT_MENTION");
        notification.setType(Notification.NotificationType.COMMENT_MENTION);
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);
        when(authClient.getUser(15)).thenThrow(new RuntimeException("auth unavailable"));

        NotificationDTO result = notificationService.createNotification(request);

        assertThat(result.getNotificationId()).isEqualTo(1);
        verify(emailService, never()).sendNotificationEmail(any(), any(), any());
    }

    @Test
    @DisplayName("Should reject invalid notification type")
    void createNotification_throwsForInvalidType() {
        request.setType("UNKNOWN_TYPE");

        assertThatThrownBy(() -> notificationService.createNotification(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid notification type");
    }

    @Test
    @DisplayName("Should retrieve notification by id")
    void getNotificationById_returnsMappedNotification() {
        when(notificationRepository.findById(1)).thenReturn(Optional.of(notification));

        assertThat(notificationService.getNotificationById(1).getMessage()).isEqualTo("Someone commented");
    }

    @Test
    @DisplayName("Should throw when notification is missing")
    void getNotificationById_throwsWhenMissing() {
        when(notificationRepository.findById(404)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.getNotificationById(404))
                .isInstanceOf(NotificationNotFoundException.class);
    }

    @Test
    @DisplayName("Should retrieve notification lists and unread count")
    void listNotifications_returnsMappedData() {
        when(notificationRepository.findByRecipientIdOrderByCreatedAtDesc(15)).thenReturn(List.of(notification));
        when(notificationRepository.findUnreadByRecipientId(15)).thenReturn(List.of(notification));
        when(notificationRepository.countUnreadByRecipientId(15)).thenReturn(1L);

        assertThat(notificationService.getNotificationsForUser(15)).hasSize(1);
        assertThat(notificationService.getUnreadNotificationsForUser(15)).hasSize(1);
        assertThat(notificationService.countUnreadNotificationsForUser(15)).isEqualTo(1L);
    }

    @Test
    @DisplayName("Should mark one or all notifications as read")
    void markRead_updatesRepository() {
        when(notificationRepository.markAsRead(1)).thenReturn(1);
        when(notificationRepository.markAllAsReadByRecipientId(15)).thenReturn(2);

        notificationService.markAsRead(1);
        notificationService.markAllAsReadForUser(15);

        verify(notificationRepository).markAsRead(1);
        verify(notificationRepository).markAllAsReadByRecipientId(15);
    }

    @Test
    @DisplayName("Should throw when mark-as-read updates no rows")
    void markAsRead_throwsWhenMissing() {
        when(notificationRepository.markAsRead(404)).thenReturn(0);

        assertThatThrownBy(() -> notificationService.markAsRead(404))
                .isInstanceOf(NotificationNotFoundException.class);
    }

    @Test
    @DisplayName("Should delete existing notification")
    void deleteNotification_deletesRecord() {
        when(notificationRepository.existsById(1)).thenReturn(true);

        notificationService.deleteNotification(1);

        verify(notificationRepository).deleteById(1);
    }

    @Test
    @DisplayName("Should broadcast to matching users")
    void broadcast_savesForMatchingRole() {
        when(authClient.getAllUsers()).thenReturn(new AuthClient.UserResponse[]{
                AuthClient.UserResponse.builder().userId(15).role("USER").build(),
                AuthClient.UserResponse.builder().userId(99).role("ADMIN").build()
        });

        notificationService.broadcast("System", "Hello", "USER");

        verify(notificationRepository, times(1)).save(any(Notification.class));
    }
}
