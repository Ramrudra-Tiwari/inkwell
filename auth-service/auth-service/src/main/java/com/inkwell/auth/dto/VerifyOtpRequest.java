package com.inkwell.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to verify OTP for password reset")
public class VerifyOtpRequest {
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Schema(description = "Email of the user", example = "user@example.com")
    private String email;

    @NotBlank(message = "OTP is required")
    @Schema(description = "6-digit OTP sent to email", example = "123456")
    private String otp;
}
