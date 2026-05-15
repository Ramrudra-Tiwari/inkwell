package com.inkwell.media;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Main entry point for the Media Microservice.
 * Enables Eureka service discovery for registration with the service registry.
 */
@SpringBootApplication
@EnableDiscoveryClient
public class MediaServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MediaServiceApplication.class, args);
    }
}

