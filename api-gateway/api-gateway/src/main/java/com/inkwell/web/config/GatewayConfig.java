package com.inkwell.web.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.inkwell.gateway.filter.AuthenticationFilter;

@Configuration
public class GatewayConfig {

    private static final String AUTH_SERVICE_URI = "http://localhost:8081";
    private static final String POST_SERVICE_URI = "http://localhost:8082";
    private static final String COMMENT_SERVICE_URI = "http://localhost:8083";
    private static final String CATEGORY_SERVICE_URI = "http://localhost:8084";
    private static final String MEDIA_SERVICE_URI = "http://localhost:8085";
    private static final String PAYMENT_SERVICE_URI = "http://localhost:8086";
    private static final String MESSAGING_SERVICE_URI = "http://localhost:8087";

    private final AuthenticationFilter authenticationFilter;

    public GatewayConfig(AuthenticationFilter authenticationFilter) {
        this.authenticationFilter = authenticationFilter;
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("auth-service", route -> route.path("/auth", "/auth/**")
                        .uri(AUTH_SERVICE_URI))
                .route("auth-oauth-service", route -> route.path("/oauth2/**", "/login/oauth2/**")
                        .uri(AUTH_SERVICE_URI))
                .route("user-service", route -> route.path("/user", "/user/**")
                        .uri(AUTH_SERVICE_URI))
                .route("admin-user-service", route -> route.path("/users", "/users/**")
                        .uri(AUTH_SERVICE_URI))
                .route("post-service", route -> route.path("/posts", "/posts/**")
                        .filters(filter -> filter.rewritePath("/posts(?<segment>/?.*)", "/api/v1/posts${segment}"))
                        .uri(POST_SERVICE_URI))
                .route("comment-service", route -> route.path("/comments", "/comments/**")
                        .filters(filter -> filter.rewritePath("/comments(?<segment>/?.*)", "/api/v1/comments${segment}"))
                        .uri(COMMENT_SERVICE_URI))
                .route("category-service", route -> route.path("/categories", "/categories/**")
                        .filters(filter -> filter.rewritePath("/categories(?<segment>/?.*)", "/api/v1/categories${segment}"))
                        .uri(CATEGORY_SERVICE_URI))
                .route("tag-service", route -> route.path("/tags", "/tags/**")
                        .filters(filter -> filter.rewritePath("/tags(?<segment>/?.*)", "/api/v1/tags${segment}"))
                        .uri(CATEGORY_SERVICE_URI))
                .route("media-service", route -> route.path("/media", "/media/**")
                        .filters(filter -> filter.rewritePath("/media(?<segment>/?.*)", "/api/v1/media${segment}"))
                        .uri(MEDIA_SERVICE_URI))
                .route("payment-service", route -> route.path("/payments", "/payments/**")
                        .filters(filter -> filter.filter(authenticationFilter.apply(new AuthenticationFilter.Config())))
                        .uri(PAYMENT_SERVICE_URI))
                .route("newsletter-service", route -> route.path("/newsletter", "/newsletter/**")
                        .filters(filter -> filter.rewritePath("/newsletter(?<segment>/?.*)", "/api/v1/newsletter${segment}"))
                        .uri(MESSAGING_SERVICE_URI))
                .route("notification-service", route -> route.path("/notifications", "/notifications/**")
                        .filters(filter -> filter.rewritePath("/notifications(?<segment>/?.*)", "/api/v1/notifications${segment}"))
                        .uri(MESSAGING_SERVICE_URI))
                .build();
    }
}
