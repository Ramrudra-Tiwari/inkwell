package com.inkwell.post.controller;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class HomeControllerTest {

    @Test
    void homeReturnsServiceStatus() {
        assertTrue(new HomeController().home().contains("Post Service is running"));
    }
}
