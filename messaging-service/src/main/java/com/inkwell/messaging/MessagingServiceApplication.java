package com.inkwell.messaging;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Main entry point for the Messaging Microservice.
 * Enables Eureka service discovery for registration with the service registry.
 */
@SpringBootApplication
@EnableDiscoveryClient
public class MessagingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MessagingServiceApplication.class, args);
    }

    @org.springframework.context.annotation.Bean
    @org.springframework.cloud.client.loadbalancer.LoadBalanced
    public org.springframework.web.client.RestTemplate restTemplate() {
        return new org.springframework.web.client.RestTemplate();
    }
}
