package com.inkwell.messaging.controller;

import com.inkwell.messaging.service.EmailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/messaging/email")
@RequiredArgsConstructor
@Tag(name = "Email", description = "Email notification APIs")
public class EmailController {

    private final EmailService emailService;

    @PostMapping("/send-otp")
    @Operation(summary = "Send OTP email")
    public ResponseEntity<String> sendOtp(@RequestParam String email, @RequestParam String otp) {
        emailService.sendOtpEmail(email, otp);
        return ResponseEntity.ok("OTP email triggered");
    }
}
