package com.inkwell.auth.dto;

import com.inkwell.auth.entity.Provider;
import com.inkwell.auth.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for User entity, used for responses.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Integer userId;
    private String username;
    private String email;
    private String fullName;
    private Role role;
    private String bio;
    private String avatarUrl;
    private Provider provider;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
