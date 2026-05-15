package com.inkwell.auth.controller;

import com.inkwell.auth.dto.*;
import com.inkwell.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

/**
 * Controller for authentication endpoints.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication management APIs")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    public ResponseEntity<UserDTO> register(@Valid @RequestBody RegisterRequest request) {
        UserDTO user = authService.register(request);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/register/request-otp")
    @Operation(summary = "Request registration OTP")
    public ResponseEntity<String> requestRegistrationOtp(@Valid @RequestBody RegisterRequest request) {
        authService.initiateRegistration(request);
        return ResponseEntity.ok("OTP sent to your email");
    }

    @PostMapping("/register/verify-otp")
    @Operation(summary = "Verify registration OTP and create account")
    public ResponseEntity<UserDTO> verifyRegistrationOtp(@Valid @RequestBody VerifyOtpRequest request) {
        UserDTO user = authService.verifyRegistrationOtp(request);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/login")
    @Operation(summary = "Login user")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Request password reset OTP")
    public ResponseEntity<String> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ResponseEntity.ok("OTP sent to your email");
    }

    @PostMapping("/verify-otp")
    @Operation(summary = "Verify password reset OTP")
    public ResponseEntity<String> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        boolean isValid = authService.verifyOtp(request);
        return isValid ? ResponseEntity.ok("OTP verified") : ResponseEntity.badRequest().body("Invalid OTP");
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password using OTP")
    public ResponseEntity<String> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok("Password reset successful");
    }

    @GetMapping("/username/{username}")
    @Operation(summary = "Get user by username")
    public ResponseEntity<UserDTO> getUserByUsername(@PathVariable String username) {
        UserDTO user = authService.getUserByUsername(username);
        return ResponseEntity.ok(user);
    }
}
