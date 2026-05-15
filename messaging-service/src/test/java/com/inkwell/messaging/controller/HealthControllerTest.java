package com.inkwell.messaging.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Health Controller Tests")
class HealthControllerTest {

    @Test
    @DisplayName("Should expose service info")
    void getServiceInfo_returnsServiceMetadata() {
        var response = new HealthController().getServiceInfo();

        assertEquals("Messaging Service", response.getBody().get("service"));
        assertTrue(response.getBody().containsKey("endpoints"));
        assertTrue(response.getBody().containsKey("features"));
    }

    @Test
    @DisplayName("Should expose health status")
    void health_returnsUpStatus() {
        var response = new HealthController().health();

        assertEquals("messaging-service", response.getBody().get("service"));
        assertEquals("UP", response.getBody().get("status"));
    }
}
