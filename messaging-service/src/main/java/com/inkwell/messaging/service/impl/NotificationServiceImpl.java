package com.inkwell.messaging.service.impl;

import com.inkwell.messaging.dto.NotificationDTO;
import com.inkwell.messaging.dto.NotificationRequest;
import com.inkwell.messaging.entity.Notification;
import com.inkwell.messaging.exception.NotificationNotFoundException;
import com.inkwell.messaging.repository.NotificationRepository;
import com.inkwell.messaging.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of NotificationService for managing user notifications.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final com.inkwell.messaging.client.AuthClient authClient;
    private final com.inkwell.messaging.service.EmailService emailService;

    @Override
    public NotificationDTO createNotification(NotificationRequest request) {
        log.info("Creating notification for recipient: {} from actor: {}", request.getRecipientId(), request.getActorId());

        Notification.NotificationType type;
        try {
            type = Notification.NotificationType.valueOf(request.getType());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid notification type: " + request.getType());
        }

        Notification notification = Notification.builder()
                .recipientId(request.getRecipientId())
                .actorId(request.getActorId())
                .type(type)
                .title(request.getTitle())
                .message(request.getMessage())
                .isRead(false)
                .build();

        notification = notificationRepository.save(notification);

        // Trigger email notification for critical types
        try {
            if (isEmailWorthy(notification.getType())) {
                com.inkwell.messaging.client.AuthClient.UserResponse user = authClient.getUser(request.getRecipientId());
                if (user != null && user.getEmail() != null) {
                    emailService.sendNotificationEmail(
                        user.getEmail(),
                        notification.getTitle(),
                        notification.getMessage()
                    );
                    log.info("Email notification sent to: {}", user.getEmail());
                }
            }
        } catch (Exception e) {
            log.error("Failed to send email notification for ID: {}", notification.getNotificationId(), e);
        }

        log.info("Notification created with ID: {}", notification.getNotificationId());
        return mapToDTO(notification);
    }

    private boolean isEmailWorthy(Notification.NotificationType type) {
        return type == Notification.NotificationType.NEW_POST || 
               type == Notification.NotificationType.COMMENT_REPLY || 
               type == Notification.NotificationType.COMMENT_MENTION;
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationDTO getNotificationById(Integer notificationId) {
        log.debug("Getting notification by ID: {}", notificationId);

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> NotificationNotFoundException.forId(notificationId));

        return mapToDTO(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationDTO> getNotificationsForUser(Integer userId) {
        log.debug("Getting all notifications for user: {}", userId);

        List<Notification> notifications = notificationRepository.findByRecipientIdOrderByCreatedAtDesc(userId);

        return notifications.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationDTO> getUnreadNotificationsForUser(Integer userId) {
        log.debug("Getting unread notifications for user: {}", userId);

        List<Notification> notifications = notificationRepository.findUnreadByRecipientId(userId);

        return notifications.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public long countUnreadNotificationsForUser(Integer userId) {
        log.debug("Counting unread notifications for user: {}", userId);

        return notificationRepository.countUnreadByRecipientId(userId);
    }

    @Override
    public void markAsRead(Integer notificationId) {
        log.info("Marking notification as read: {}", notificationId);

        int updatedRows = notificationRepository.markAsRead(notificationId);
        if (updatedRows == 0) {
            throw NotificationNotFoundException.forId(notificationId);
        }

        log.debug("Notification marked as read: {}", notificationId);
    }

    @Override
    public void markAllAsReadForUser(Integer userId) {
        log.info("Marking all notifications as read for user: {}", userId);

        int updatedRows = notificationRepository.markAllAsReadByRecipientId(userId);

        log.info("Marked {} notifications as read for user: {}", updatedRows, userId);
    }

    @Override
    public void deleteNotification(Integer notificationId) {
        log.info("Deleting notification: {}", notificationId);

        if (!notificationRepository.existsById(notificationId)) {
            throw NotificationNotFoundException.forId(notificationId);
        }

        notificationRepository.deleteById(notificationId);

        log.debug("Notification deleted: {}", notificationId);
    }

    @Override
    public void broadcast(String title, String message, String role) {
        log.info("Broadcasting notification: {} to role: {}", title, role);
        com.inkwell.messaging.client.AuthClient.UserResponse[] users = authClient.getAllUsers();
        
        for (com.inkwell.messaging.client.AuthClient.UserResponse user : users) {
            if (role == null || "ALL".equalsIgnoreCase(role) || role.equalsIgnoreCase(user.getRole())) {
                try {
                    Notification notification = Notification.builder()
                            .recipientId(user.getUserId())
                            .type(Notification.NotificationType.SYSTEM_ALERT)
                            .title(title)
                            .message(message)
                            .isRead(false)
                            .build();
                    notificationRepository.save(notification);
                } catch (Exception e) {
                    log.error("Failed to broadcast notification to user: {}", user.getUserId(), e);
                }
            }
        }
    }

    private NotificationDTO mapToDTO(Notification notification) {
        return NotificationDTO.builder()
                .notificationId(notification.getNotificationId())
                .recipientId(notification.getRecipientId())
                .actorId(notification.getActorId())
                .type(notification.getType().name())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .isRead(notification.getIsRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
