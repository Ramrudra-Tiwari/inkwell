package com.inkwell.auth.config;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StreamUtils;

import org.springframework.web.client.RestTemplate;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

/**
 * SecurityConfig — clean rewrite.
 *
 * Strategy:
 *   - Let Spring Boot auto-configure the ClientRegistrationRepository
 *     from spring.security.oauth2.client.registration.* in application.yml.
 *   - Only configure the SecurityFilterChain and a logging token client.
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.disable())
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable());

        http.oauth2Login(oauth -> oauth
            .successHandler(oAuth2SuccessHandler)
            .failureHandler((request, response, exception) -> {
                log.error("OAuth2 Failure Handler : Login Failed: {}", exception.getMessage(), exception);
                String reason = exception != null && exception.getMessage() != null
                        ? URLEncoder.encode(exception.getMessage(), StandardCharsets.UTF_8)
                        : "oauth_failed";
                response.sendRedirect(frontendUrl + "/login?error=true&reason=" + reason);
            })
        );

        http
            .sessionManagement(session ->
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers(antMatcher(org.springframework.http.HttpMethod.GET, "/api/v1/users/*")).permitAll()
                    .requestMatchers(antMatcher(org.springframework.http.HttpMethod.GET, "/api/v1/users/*/follower-count")).permitAll()
                    .requestMatchers(
                            "/auth/**",
                            "/api/v1/auth/**",
                            "/oauth2/**",
                            "/login/**",
                            "/swagger-ui/**",
                            "/swagger-ui.html",
                            "/api-docs/**",
                            "/v3/api-docs/**"
                    ).permitAll()
                    .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex
                    .authenticationEntryPoint((req, res, e) ->
                            res.sendError(HttpServletResponse.SC_UNAUTHORIZED, e.getMessage())
                    )
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
