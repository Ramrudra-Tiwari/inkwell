package com.inkwell.gateway.filter;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inkwell.gateway.util.JwtService;

import reactor.core.publisher.Mono;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    @Autowired
    private JwtService jwtService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public AuthenticationFilter() {
        super(Config.class);
    }

    public static class Config {}

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String path = exchange.getRequest().getURI().getPath();
            HttpMethod method = exchange.getRequest().getMethod();

            if (isPublicPath(path, method)) {
                return chain.filter(exchange);
            }

            if (!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                return onError(exchange, "Missing Authorization Header", HttpStatus.UNAUTHORIZED, path);
            }

            String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return onError(exchange, "Invalid Authorization Header Format", HttpStatus.UNAUTHORIZED, path);
            }

            String token = authHeader.substring(7);

            try {
                String email = jwtService.extractEmail(token);

                if (jwtService.isTokenExpired(token)) {
                    return onError(exchange, "Token Expired", HttpStatus.UNAUTHORIZED, path);
                }

                return chain.filter(exchange.mutate()
                        .request(exchange.getRequest().mutate()
                                .header("X-User-Email", email)
                                .build())
                        .build());
            } catch (Exception e) {
                return onError(exchange, "Invalid Token", HttpStatus.UNAUTHORIZED, path);
            }
        };
    }

    private boolean isPublicPath(String path, HttpMethod method) {
        boolean isGet = HttpMethod.GET.equals(method);
        boolean isPost = HttpMethod.POST.equals(method);

        return path.startsWith("/auth") ||
                path.startsWith("/api/v1/auth") ||
                path.startsWith("/oauth2") ||
                path.startsWith("/login") ||
                path.startsWith("/media") ||
                path.startsWith("/api/v1/media") ||
                path.startsWith("/posts/published") ||
                path.startsWith("/api/v1/posts/published") ||
                (isGet && path.startsWith("/api/v1/posts/slug/")) ||
                (isGet && path.matches("/api/v1/posts/\\d+")) ||
                (isGet && path.startsWith("/api/v1/posts/search")) ||
                (isPost && path.matches("/api/v1/posts/\\d+/view")) ||
                (isGet && path.matches("/api/v1/users/\\d+")) ||
                path.startsWith("/api/v1/posts/analytics") ||
                path.startsWith("/api/v1/taxonomy") ||
                path.startsWith("/api/v1/categories") ||
                path.startsWith("/api/v1/tags") ||
                (isGet && path.startsWith("/api/v1/comments/post/")) ||
                (isGet && path.matches("/api/v1/comments/\\d+/replies")) ||
                path.startsWith("/categories") ||
                path.startsWith("/tags") ||
                path.startsWith("/search") ||
                path.startsWith("/swagger-ui") ||
                path.startsWith("/v3/api-docs") ||
                path.startsWith("/h2-console");
    }

    private Mono<Void> onError(org.springframework.web.server.ServerWebExchange exchange,
                               String message,
                               HttpStatus status,
                               String path) {

        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        try {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("timestamp", LocalDateTime.now().toString());
            errorResponse.put("status", status.value());
            errorResponse.put("error", status.getReasonPhrase());
            errorResponse.put("message", message);
            errorResponse.put("path", path);

            byte[] bytes = objectMapper.writeValueAsBytes(errorResponse);

            return exchange.getResponse()
                    .writeWith(Mono.just(exchange.getResponse()
                            .bufferFactory()
                            .wrap(bytes)));
        } catch (Exception e) {
            return exchange.getResponse().setComplete();
        }
    }
}
