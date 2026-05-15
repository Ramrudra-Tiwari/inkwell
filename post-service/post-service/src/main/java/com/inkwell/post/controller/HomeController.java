package com.inkwell.post.controller;


import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "Post Service is running 🚀";
    }
}