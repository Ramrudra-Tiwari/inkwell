package com.inkwell.media.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Root controller for handling requests to the root path.
 * Prevents 404 errors for favicon and root access.
 */
@RestController
@Slf4j
public class RootController {

    /**
     * Root endpoint - redirects to API health check.
     */
    @GetMapping("/")
    public ResponseEntity<Void> root() {
        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, "/api/v1/media/health")
                .build();
    }

    /**
     * Favicon endpoint - returns 404 to prevent error logs.
     */
    @GetMapping("/favicon.ico")
    public ResponseEntity<Void> favicon() {
        return ResponseEntity.notFound().build();
    }
}
