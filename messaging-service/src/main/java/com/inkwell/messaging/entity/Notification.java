package com.inkwell.messaging.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Notification entity representing user notifications.
 */
@Entity
@Table(name = "notification", indexes = {
    @Index(name = "idx_recipient_id", columnList = "recipient_id"),
    @Index(name = "idx_actor_id", columnList = "actor_id"),
    @Index(name = "idx_type", columnList = "type"),
    @Index(name = "idx_is_read", columnList = "is_read"),
    @Index(name = "idx_created_at", columnList = "created_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer notificationId;

    @Column(name = "recipient_id", nullable = false)
    private Integer recipientId;

    @Column(name = "actor_id")
    private Integer actorId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, length = 1000)
    private String message;

    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private Boolean isRead = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public enum NotificationType {
        NEW_COMMENT,
        REPLY,
        MENTION,
        NEW_POST,
        SYSTEM_ALERT,
        DONATION_RECEIVED,
        COMMENT_REPLY,
        COMMENT_MENTION
    }
}
