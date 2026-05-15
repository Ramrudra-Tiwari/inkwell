package com.inkwell.auth.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class MiscControllerTest {

    @Test
    void healthAndSecureEndpointsReturnText() {
        assertEquals("Auth Service is UP", new HealthController().health());
        assertEquals("You are authenticated!", new TestController().secure());
    }

    @Test
    void oauthFallbackRedirectsWhenGoogleOauthIsNotConfigured() throws Exception {
        OAuth2FallbackController controller = new OAuth2FallbackController();
        HttpServletResponse response = mock(HttpServletResponse.class);
        ReflectionTestUtils.setField(controller, "frontendUrl", "http://localhost:5173");
        ReflectionTestUtils.setField(controller, "googleClientId", "");
        ReflectionTestUtils.setField(controller, "googleClientSecret", "");

        controller.googleAuthorization(response);

        verify(response).sendRedirect(contains("http://localhost:5173/login?error=true"));
    }

    @Test
    void oauthFallbackReturnsNotFoundWhenGoogleOauthIsConfigured() throws Exception {
        OAuth2FallbackController controller = new OAuth2FallbackController();
        HttpServletResponse response = mock(HttpServletResponse.class);
        ReflectionTestUtils.setField(controller, "googleClientId", "client-id");
        ReflectionTestUtils.setField(controller, "googleClientSecret", "client-secret");

        controller.googleAuthorization(response);

        verify(response).sendError(HttpServletResponse.SC_NOT_FOUND);
    }
}
