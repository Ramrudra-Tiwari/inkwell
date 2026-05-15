package com.inkwell.messaging.controller;

import com.inkwell.messaging.service.EmailService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("Email Controller Tests")
class EmailControllerTest {

    @Mock
    private EmailService emailService;

    @Test
    @DisplayName("Should trigger OTP email")
    void sendOtp_delegatesToEmailService() {
        EmailController controller = new EmailController(emailService);

        assertEquals("OTP email triggered", controller.sendOtp("user@example.com", "123456").getBody());
        verify(emailService).sendOtpEmail("user@example.com", "123456");
    }
}
