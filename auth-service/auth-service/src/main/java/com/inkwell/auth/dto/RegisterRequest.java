package com.inkwell.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO for user registration request.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload used to register a new InkWell user.")
public class RegisterRequest {
    @Schema(description = "Public username chosen by the user.", example = "inkwell-writer", requiredMode = Schema.RequiredMode.REQUIRED)
    private String username;
    @Schema(description = "Email address used for login and notifications.", example = "writer@inkwell.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;
    @Schema(description = "Password chosen during registration.", example = "secret123", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[!@#$%^&*()_+{}\\[\\]:;<>,.?~\\\\/-]).{6,}$", message = "Password must include at least one number and one special character")
    private String password;
    @Schema(description = "Full name displayed on the profile.", example = "InkWell Writer", requiredMode = Schema.RequiredMode.REQUIRED)
    private String fullName;
}
