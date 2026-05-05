package com.example.chatwavebackend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class CorsConfig {

    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // Only allow your frontend
        config.setAllowedOrigins(List.of(allowedOrigins.split(",")));

        // Only allow these methods
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH"));

        // Allow these headers (especially important for JWT auth)
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));

        // Allow cookies/auth headers
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}