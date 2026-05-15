package com.inkwell.messaging.service;

import com.inkwell.messaging.dto.NotificationDTO;
import com.inkwell.messaging.dto.NotificationRequest;

import java.util.List;

/**
 * Service interface for notification operations.
 */
public interface NotificationService {

    /**
     * Create a new notification.
     */
    NotificationDTO createNotification(NotificationRequest request);

    /**
     * Get notification by ID.
     */
    NotificationDTO getNotificationById(Integer notificationId);

    /**
     * Get all notifications for a user.
     */
    List<NotificationDTO> getNotificationsForUser(Integer userId);

    /**
     * Get unread notifications for a user.
     */
    List<NotificationDTO> getUnreadNotificationsForUser(Integer userId);

    /**
     * Count unread notifications for a user.
     */
    long countUnreadNotificationsForUser(Integer userId);

    /**
     * Mark notification as read.
     */
    void markAsRead(Integer notificationId);

    /**
     * Mark all notifications as read for a user.
     */
    void markAllAsReadForUser(Integer userId);

    /**
     * Delete notification.
     */
    void deleteNotification(Integer notificationId);

    /**
     * Broadcast notification to all users or by role.
     */
    void broadcast(String title, String message, String role);
}
