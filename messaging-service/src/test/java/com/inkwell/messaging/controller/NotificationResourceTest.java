package com.inkwell.messaging.controller;

import com.inkwell.messaging.dto.NotificationDTO;
import com.inkwell.messaging.dto.NotificationRequest;
import com.inkwell.messaging.service.NotificationService;
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
@DisplayName("Notification Resource Tests")
class NotificationResourceTest {

    @Mock
    private NotificationService notificationService;

    private NotificationResource resource;
    private NotificationDTO dto;

    @BeforeEach
    void setUp() {
        resource = new NotificationResource(notificationService);
        dto = NotificationDTO.builder()
                .notificationId(1)
                .recipientId(15)
                .type("SYSTEM_ALERT")
                .title("Title")
                .message("Message")
                .isRead(false)
                .build();
    }

    @Test
    @DisplayName("Should create and read notifications")
    void readEndpoints_delegateToService() {
        NotificationRequest request = NotificationRequest.builder()
                .recipientId(15)
                .actorId(6)
                .type("SYSTEM_ALERT")
                .title("Title")
                .message("Message")
                .build();
        when(notificationService.createNotification(request)).thenReturn(dto);
        when(notificationService.getNotificationById(1)).thenReturn(dto);
        when(notificationService.getNotificationsForUser(15)).thenReturn(List.of(dto));
        when(notificationService.getUnreadNotificationsForUser(15)).thenReturn(List.of(dto));
        when(notificationService.countUnreadNotificationsForUser(15)).thenReturn(1L);

        assertEquals(HttpStatus.CREATED, resource.createNotification(request).getStatusCode());
        assertEquals(1, resource.getNotificationById(1).getBody().getNotificationId());
        assertEquals(1, resource.getNotificationsForUser(15).getBody().size());
        assertEquals(1, resource.getUnreadNotificationsForUser(15).getBody().size());
        assertEquals(1L, resource.countUnreadNotificationsForUser(15).getBody());
    }

    @Test
    @DisplayName("Should update, delete, and broadcast notifications")
    void mutationEndpoints_delegateToService() {
        assertEquals(HttpStatus.OK, resource.markAsRead(1).getStatusCode());
        assertEquals(HttpStatus.OK, resource.markAllAsReadForUser(15).getStatusCode());
        assertEquals(HttpStatus.NO_CONTENT, resource.deleteNotification(1).getStatusCode());
        assertEquals(HttpStatus.OK, resource.broadcast("Title", "Message", "USER").getStatusCode());

        verify(notificationService).markAsRead(1);
        verify(notificationService).markAllAsReadForUser(15);
        verify(notificationService).deleteNotification(1);
        verify(notificationService).broadcast("Title", "Message", "USER");
    }
}
