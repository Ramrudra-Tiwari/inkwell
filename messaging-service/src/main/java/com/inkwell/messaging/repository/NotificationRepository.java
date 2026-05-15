package com.inkwell.messaging.repository;

import com.inkwell.messaging.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for Notification entity operations.
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Integer> {

    /**
     * Find all notifications for a recipient.
     */
    List<Notification> findByRecipientIdOrderByCreatedAtDesc(Integer recipientId);

    /**
     * Find unread notifications for a recipient.
     */
    @Query("SELECT n FROM Notification n WHERE n.recipientId = :recipientId AND n.isRead = false ORDER BY n.createdAt DESC")
    List<Notification> findUnreadByRecipientId(@Param("recipientId") Integer recipientId);

    /**
     * Count unread notifications for a recipient.
     */
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.recipientId = :recipientId AND n.isRead = false")
    long countUnreadByRecipientId(@Param("recipientId") Integer recipientId);

    /**
     * Mark notification as read.
     */
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.notificationId = :notificationId")
    int markAsRead(@Param("notificationId") Integer notificationId);

    /**
     * Mark all notifications as read for a recipient.
     */
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.recipientId = :recipientId AND n.isRead = false")
    int markAllAsReadByRecipientId(@Param("recipientId") Integer recipientId);

    /**
     * Find notifications by type for a recipient.
     */
    List<Notification> findByRecipientIdAndTypeOrderByCreatedAtDesc(Integer recipientId, Notification.NotificationType type);
}
