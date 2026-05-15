package com.inkwell.auth.controller;

import com.inkwell.auth.dto.LoginRequest;
import com.inkwell.auth.dto.LoginResponse;
import com.inkwell.auth.dto.ForgotPasswordRequest;
import com.inkwell.auth.dto.RegisterRequest;
import com.inkwell.auth.dto.ResetPasswordRequest;
import com.inkwell.auth.dto.UserDTO;
import com.inkwell.auth.dto.VerifyOtpRequest;
import com.inkwell.auth.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthControllerTest {

    @Test
    void register_returnsUserFromService() {
        AuthService authService = mock(AuthService.class);
        AuthController controller = new AuthController(authService);
        RegisterRequest request = RegisterRequest.builder().email("a@b.com").username("author").password("secret").build();
        UserDTO user = UserDTO.builder().userId(1).email("a@b.com").build();
        when(authService.register(request)).thenReturn(user);

        ResponseEntity<UserDTO> response = controller.register(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(user, response.getBody());
    }

    @Test
    void requestRegistrationOtp_returnsSuccessMessage() {
        AuthService authService = mock(AuthService.class);
        AuthController controller = new AuthController(authService);
        RegisterRequest request = RegisterRequest.builder()
                .email("a@b.com")
                .username("author")
                .password("secret")
                .fullName("Author Name")
                .build();

        ResponseEntity<String> response = controller.requestRegistrationOtp(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("OTP sent to your email", response.getBody());
    }

    @Test
    void verifyRegistrationOtp_returnsCreatedUser() {
        AuthService authService = mock(AuthService.class);
        AuthController controller = new AuthController(authService);
        VerifyOtpRequest request = new VerifyOtpRequest("a@b.com", "123456");
        UserDTO user = UserDTO.builder().userId(1).email("a@b.com").build();
        when(authService.verifyRegistrationOtp(request)).thenReturn(user);

        ResponseEntity<UserDTO> response = controller.verifyRegistrationOtp(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(user, response.getBody());
    }

    @Test
    void login_returnsLoginResponseFromService() {
        AuthService authService = mock(AuthService.class);
        AuthController controller = new AuthController(authService);
        LoginRequest request = new LoginRequest("a@b.com", "secret");
        LoginResponse loginResponse = LoginResponse.builder().token("token").build();
        when(authService.login(request)).thenReturn(loginResponse);

        ResponseEntity<LoginResponse> response = controller.login(request);

        assertEquals("token", response.getBody().getToken());
    }

    @Test
    void verifyOtp_returnsOkWhenOtpIsValid() {
        AuthService authService = mock(AuthService.class);
        AuthController controller = new AuthController(authService);
        VerifyOtpRequest request = new VerifyOtpRequest("a@b.com", "123456");
        when(authService.verifyOtp(request)).thenReturn(true);

        assertEquals(HttpStatus.OK, controller.verifyOtp(request).getStatusCode());
    }

    @Test
    void passwordAndUsernameEndpointsDelegateToService() {
        AuthService authService = mock(AuthService.class);
        AuthController controller = new AuthController(authService);
        ForgotPasswordRequest forgot = new ForgotPasswordRequest("a@b.com");
        ResetPasswordRequest reset = new ResetPasswordRequest("a@b.com", "123456", "New@123");
        UserDTO user = UserDTO.builder().username("author").build();
        when(authService.getUserByUsername("author")).thenReturn(user);

        assertEquals("OTP sent to your email", controller.forgotPassword(forgot).getBody());
        assertEquals("Password reset successful", controller.resetPassword(reset).getBody());
        assertEquals(user, controller.getUserByUsername("author").getBody());
    }
}
