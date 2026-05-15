package com.inkwell.comment.controller;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class HomeControllerTest {

    @Test
    void home_returnsServiceStatusMessage() {
        String response = new HomeController().home();

        assertTrue(response.contains("Comment Service is running"));
    }
}
