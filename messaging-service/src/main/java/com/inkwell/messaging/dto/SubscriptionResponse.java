package com.inkwell.messaging.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for subscription response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionResponse {
    private Integer subscriberId;
    private String email;
    private String status;
    private String message;
    private Integer statusCode;
}
