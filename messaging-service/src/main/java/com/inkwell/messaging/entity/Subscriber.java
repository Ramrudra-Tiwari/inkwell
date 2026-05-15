package com.inkwell.messaging.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Subscriber entity representing newsletter subscribers.
 */
@Entity
@Table(name = "subscriber", indexes = {
    @Index(name = "idx_email", columnList = "email"),
    @Index(name = "idx_token", columnList = "token"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_subscribed_at", columnList = "subscribed_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Subscriber {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer subscriberId;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "user_id")
    private Integer userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private SubscriberStatus status = SubscriberStatus.PENDING;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(length = 1000)
    private String preferences;

    @Column(name = "subscribed_at", nullable = false, updatable = false)
    private LocalDateTime subscribedAt;

    @PrePersist
    protected void onCreate() {
        if (subscribedAt == null) {
            subscribedAt = LocalDateTime.now();
        }
    }

    public enum SubscriberStatus {
        PENDING,
        ACTIVE,
        UNSUBSCRIBED
    }
}
