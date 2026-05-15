package com.inkwell.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Credentials used to sign in to InkWell.")
public class LoginRequest {
    @Schema(description = "Registered email address of the user.", example = "author@inkwell.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;
    @Schema(description = "Plain-text password submitted for authentication.", example = "secret123", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;
}
