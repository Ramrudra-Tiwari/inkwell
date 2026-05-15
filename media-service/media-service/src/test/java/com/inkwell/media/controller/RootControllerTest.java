package com.inkwell.media.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Root Controller Tests")
class RootControllerTest {

    private final RootController rootController = new RootController();

    @Test
    @DisplayName("Should redirect root to media health endpoint")
    void root_redirectsToHealth() {
        ResponseEntity<Void> response = rootController.root();

        assertEquals(HttpStatus.FOUND, response.getStatusCode());
        assertEquals("/api/v1/media/health", response.getHeaders().getFirst("Location"));
    }

    @Test
    @DisplayName("Should return not found for favicon")
    void favicon_returnsNotFound() {
        assertEquals(HttpStatus.NOT_FOUND, rootController.favicon().getStatusCode());
    }
}
