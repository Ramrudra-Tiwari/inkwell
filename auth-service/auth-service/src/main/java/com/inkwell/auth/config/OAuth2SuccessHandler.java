package com.inkwell.auth.config;

import com.inkwell.auth.entity.Provider;
import com.inkwell.auth.entity.Role;
import com.inkwell.auth.entity.User;
import com.inkwell.auth.repository.UserRepository;
import com.inkwell.auth.service.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        System.out.println("Success Handler called");

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String registrationId = "google";
        if (authentication instanceof org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken token) {
            registrationId = token.getAuthorizedClientRegistrationId();
        }

        String email = getStringAttribute(oAuth2User, "email");
        if (email == null && "github".equalsIgnoreCase(registrationId)) {
            email = getStringAttribute(oAuth2User, "login") + "@github.com";
        }

        if (email == null) {
            response.sendRedirect(frontendUrl + "/login?error=true&reason=no_email");
            return;
        }

        String fullName = Optional.ofNullable(getStringAttribute(oAuth2User, "name")).orElse(email);
        String usernameBase = email.contains("@") ? email.substring(0, email.indexOf('@')) : email;
        Provider provider = "github".equalsIgnoreCase(registrationId) ? Provider.GITHUB : Provider.GOOGLE;

        String finalEmail = email;
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> userRepository.save(User.builder()
                        .username(generateUniqueUsername(usernameBase))
                        .email(finalEmail)
                        .passwordHash(passwordEncoder.encode(java.util.UUID.randomUUID().toString()))
                        .fullName(fullName)
                        .role(Role.AUTHOR)
                        .provider(provider)
                        .isActive(true)
                        .createdAt(LocalDateTime.now())
                        .build()));

        if (user.getRole() == null || user.getRole() == Role.READER) {
            user.setRole(Role.AUTHOR);
            user = userRepository.save(user);
        }

        String role = user.getRole() != null ? user.getRole().name() : Role.AUTHOR.name();
        String token = jwtUtils.generateToken(email, role);

        String redirectUrl = frontendUrl + "/?token=" + urlEncode(token)
                + "&userId=" + user.getUserId()
                + "&username=" + urlEncode(user.getUsername())
                + "&email=" + urlEncode(user.getEmail())
                + "&fullName=" + urlEncode(user.getFullName())
                + "&role=" + urlEncode(role)
                + "&provider=" + urlEncode(user.getProvider().name())
                + "&isActive=" + user.getIsActive();

        response.sendRedirect(redirectUrl);
    }

    private String generateUniqueUsername(String baseUsername) {
        String sanitized = baseUsername == null || baseUsername.isBlank()
                ? "google_user"
                : baseUsername.replaceAll("[^a-zA-Z0-9._-]", "").trim();

        if (sanitized.isBlank()) {
            sanitized = "google_user";
        }

        String candidate = sanitized;
        int suffix = 1;

        while (userRepository.findByUsername(candidate).isPresent()) {
            candidate = sanitized + suffix;
            suffix++;
        }

        return candidate;
    }

    private String urlEncode(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }

    private String getStringAttribute(OAuth2User user, String attributeName) {
        Object value = user.getAttributes().get(attributeName);
        return value != null ? String.valueOf(value) : null;
    }
}
