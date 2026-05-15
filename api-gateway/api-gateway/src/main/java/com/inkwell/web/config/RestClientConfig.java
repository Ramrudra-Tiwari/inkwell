package com.inkwell.web.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Shared HTTP client configuration for inter-service calls from MVC controllers
 * and any future aggregation services.
 */
@Configuration
public class RestClientConfig {

    private RestTemplate createRestTemplate() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(3000);
        requestFactory.setReadTimeout(5000);
        return new RestTemplate(requestFactory);
    }

    @Bean
    public RestTemplate restTemplate() {
        return createRestTemplate();
    }

    @Bean
    @Qualifier("authRestTemplate")
    public RestTemplate authRestTemplate() {
        return createRestTemplate();
    }

    @Bean
    @Qualifier("postRestTemplate")
    public RestTemplate postRestTemplate() {
        return createRestTemplate();
    }

    @Bean
    @Qualifier("commentRestTemplate")
    public RestTemplate commentRestTemplate() {
        return createRestTemplate();
    }

    @Bean
    @Qualifier("categoryRestTemplate")
    public RestTemplate categoryRestTemplate() {
        return createRestTemplate();
    }

    @Bean
    @Qualifier("mediaRestTemplate")
    public RestTemplate mediaRestTemplate() {
        return createRestTemplate();
    }

    @Bean
    @Qualifier("newsletterRestTemplate")
    public RestTemplate newsletterRestTemplate() {
        return createRestTemplate();
    }

    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }
}
