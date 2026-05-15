package com.inkwell.category;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Category & Tag Microservice for InkWell Blogging Platform.
 * Handles hierarchical categories and tags with post association.
 */
@SpringBootApplication
@EnableDiscoveryClient
public class CategoryServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(CategoryServiceApplication.class, args);
	}

}

