package com.inkwell.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration("gatewaySecurityConfig")
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeExchange(exchanges -> exchanges
                .anyExchange().permitAll()
            );
        return http.build();
    }
// .authorizeExchange(exchanges -> exchanges
//    .pathMatchers("/api/v1/auth/**").permitAll()  // login/register
//    .anyExchange().authenticated()               // baki secured
//)
//upper koi v api access kr sakta h without login
// sb apis access kr sakta h so we can also use this
// this will only allow login register but block other and make them secure

    @Bean
    public org.springframework.web.cors.reactive.CorsConfigurationSource corsConfigurationSource() {
        org.springframework.web.cors.CorsConfiguration corsConfig = new org.springframework.web.cors.CorsConfiguration();
        corsConfig.setAllowedOrigins(java.util.Arrays.asList(
            "http://localhost:5173", 
            "http://localhost:5174",
            "http://127.0.0.1:5173",
            "http://127.0.0.1:5174"
        ));
        corsConfig.setMaxAge(3600L);
        corsConfig.setAllowedMethods(java.util.Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        corsConfig.setAllowedHeaders(java.util.Arrays.asList("*"));
        corsConfig.setAllowCredentials(true);

        org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource source = new org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);   // areh bhai sb ke liye apply hoge
        return source;
    }
}
