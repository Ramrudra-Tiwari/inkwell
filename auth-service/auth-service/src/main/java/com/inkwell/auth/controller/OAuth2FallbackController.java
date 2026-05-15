package com.inkwell.auth.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
public class OAuth2FallbackController {

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    @Value("${app.oauth2.google.client-id:}")
    private String googleClientId;

    @Value("${app.oauth2.google.client-secret:}")
    private String googleClientSecret;

    @GetMapping("/oauth2/authorization/google")
    public void googleAuthorization(HttpServletResponse response) throws IOException {
        if (isGoogleOauthConfigured()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String reason = URLEncoder.encode(
                "Google sign-in is not configured on the server. Add GOOGLE_CLIENT_ID and GOOGLE_CLIENT_SECRET first.",
                StandardCharsets.UTF_8
        );
        response.sendRedirect(frontendUrl + "/login?error=true&reason=" + reason);
    }

    private boolean isGoogleOauthConfigured() {
        return StringUtils.hasText(googleClientId) && StringUtils.hasText(googleClientSecret);
    }
}
