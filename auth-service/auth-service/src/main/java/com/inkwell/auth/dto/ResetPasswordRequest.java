package com.inkwell.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to reset password using OTP")
public class ResetPasswordRequest {
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Schema(description = "Email of the user", example = "user@example.com")
    private String email;

    @NotBlank(message = "OTP is required")
    @Schema(description = "6-digit OTP sent to email", example = "123456")
    private String otp;

    @NotBlank(message = "New password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[!@#$%^&*()_+{}\\[\\]:;<>,.?~\\\\/-]).{6,}$", message = "Password must include at least one number and one special character")
    @Schema(description = "New password to set", example = "NewSecret@123")
    private String newPassword;
}
