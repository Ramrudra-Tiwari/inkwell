package com.inkwell.web.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(ServerHttpSecurity.CorsSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(org.springframework.http.HttpMethod.OPTIONS).permitAll()
                        .pathMatchers(org.springframework.http.HttpMethod.GET,
                                "/api/v1/posts/published",
                                "/api/v1/posts/slug/**",
                                "/api/v1/posts/search",
                                "/api/v1/posts/{postId:[0-9]+}",
                                "/api/v1/users/{userId:[0-9]+}",
                                "/api/v1/posts/analytics/**",
                                "/api/v1/comments/post/**",
                                "/api/v1/comments/{commentId:[0-9]+}/replies"
                        ).permitAll()
                        .pathMatchers(org.springframework.http.HttpMethod.POST,
                                "/api/v1/posts/{postId:[0-9]+}/view"
                        ).permitAll()
                        .pathMatchers("/admin/**").hasRole("ADMIN")
                        .pathMatchers("/author/**").hasAnyRole("AUTHOR", "ADMIN")
                        .pathMatchers(
                                "/",
                                "/favicon.ico",
                                "/blog/**",
                                "/search",
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs",
                                "/v3/api-docs/**",
                                "/webjars/**",
                                "/auth",
                                "/auth/**",
                                "/oauth2/**",
                                "/login/oauth2/**",
                                "/posts",
                                "/posts/**",
                                "/comments",
                                "/comments/**",
                                "/categories",
                                "/categories/**",
                                "/tags",
                                "/tags/**",
                                "/media",
                                "/media/**",
                                "/api/v1/media/**",
                                "/api/v1/posts/published",
                                "/newsletter",
                                "/newsletter/**",
                                "/notifications",
                                "/notifications/**",
                                "/payments",
                                "/payments/**",
                                "/css/**",
                                "/js/**"
                        ).permitAll()
                        .anyExchange().authenticated()
                )
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint((exchange, e) -> {
                            exchange.getResponse().setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
                            return exchange.getResponse().setComplete();
                        })
                )
                .build();
    }
}
