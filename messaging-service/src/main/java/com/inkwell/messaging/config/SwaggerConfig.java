package com.inkwell.messaging.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger/OpenAPI configuration for API documentation.
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI messagingServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Messaging Service API")
                        .description("API for newsletter subscriptions and user notifications in InkWell Blogging Platform")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("InkWell Development Team")
                                .email("dev@inkwell.com")));
    }
}
