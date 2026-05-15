package com.inkwell.messaging.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for Subscriber entity.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriberDTO {
    private Integer subscriberId;
    private String email;
    private Integer userId;
    private String status;
    private String token;
    private String preferences;
    private LocalDateTime subscribedAt;
}
