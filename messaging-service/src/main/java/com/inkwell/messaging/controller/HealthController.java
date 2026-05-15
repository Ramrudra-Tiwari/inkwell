package com.inkwell.messaging.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Health check controller for service monitoring.
 */
@RestController
@Slf4j
@Tag(name = "Health Check", description = "Service health monitoring endpoints")
public class HealthController {

    @GetMapping("/")
    @Operation(summary = "Service information", description = "Get messaging service information and available endpoints")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Service information retrieved successfully")
    })
    public ResponseEntity<Map<String, Object>> getServiceInfo() {
        log.debug("Service info requested");

        Map<String, Object> response = new HashMap<>();
        response.put("service", "Messaging Service");
        response.put("status", "UP");
        response.put("version", "1.0.0");
        response.put("description", "InkWell Blogging Platform - Newsletter & Notification Service");
        response.put("endpoints", Map.of(
            "api_documentation", "http://localhost:8086/swagger-ui/index.html",
            "api_spec", "http://localhost:8086/v3/api-docs",
            "health_check", "http://localhost:8086/api/v1/health",
            "newsletter_api", "http://localhost:8086/api/v1/newsletter",
            "notifications_api", "http://localhost:8086/api/v1/notifications"
        ));
        response.put("features", new String[]{
            "Newsletter subscription with double opt-in flow",
            "Real-time notification management",
            "RabbitMQ message processing",
            "Email confirmation (mocked for development)"
        });
        response.put("timestamp", LocalDateTime.now());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/v1/health")
    @Operation(summary = "Health check", description = "Check if the messaging service is running")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Service is healthy")
    })
    public ResponseEntity<Map<String, Object>> health() {
        log.debug("Health check requested");

        Map<String, Object> health = new HashMap<>();
        health.put("service", "messaging-service");
        health.put("status", "UP");
        health.put("timestamp", LocalDateTime.now());
        health.put("version", "1.0.0");

        return ResponseEntity.ok(health);
    }
}
