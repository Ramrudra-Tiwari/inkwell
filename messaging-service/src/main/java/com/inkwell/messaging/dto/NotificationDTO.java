package com.inkwell.messaging.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for Notification entity.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {
    private Integer notificationId;
    private Integer recipientId;
    private Integer actorId;
    private String type;
    private String title;
    private String message;
    private Boolean isRead;
    private LocalDateTime createdAt;
}
