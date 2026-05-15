package com.inkwell.category.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Home Controller Tests")
class HomeControllerTest {

    @Test
    @DisplayName("Should return service status text")
    void home_returnsStatus() {
        assertTrue(new HomeController().home().contains("Category & Tag Service is running"));
    }
}
