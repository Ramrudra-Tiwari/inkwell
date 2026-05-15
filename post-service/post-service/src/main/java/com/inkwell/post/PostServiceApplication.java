package com.inkwell.post;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Post Service Application - Main entry point for the Post Microservice
 */
@SpringBootApplication
@EnableDiscoveryClient
public class PostServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(PostServiceApplication.class, args);
	}

	@org.springframework.context.annotation.Bean
	@org.springframework.cloud.client.loadbalancer.LoadBalanced
	public org.springframework.web.client.RestTemplate restTemplate() {
		return new org.springframework.web.client.RestTemplate();
	}
}

