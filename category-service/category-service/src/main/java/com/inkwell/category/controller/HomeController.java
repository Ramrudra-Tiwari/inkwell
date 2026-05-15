package com.inkwell.category.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Home controller for the Category & Tag Service.
 * Provides a simple health check endpoint.
 */
@RestController
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "Category & Tag Service is running 🚀";
    }
}

