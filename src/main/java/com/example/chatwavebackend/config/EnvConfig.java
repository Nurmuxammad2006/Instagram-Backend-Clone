package com.example.chatwavebackend.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Configuration;

/**
 * Load environment variables from .env file at application startup
 */
@Configuration
public class EnvConfig {

    static {
        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing() // Won't fail if .env not found (useful in production)
                .load();

        // Load all variables from .env into System properties
        dotenv.entries().forEach(entry ->
            System.setProperty(entry.getKey(), entry.getValue())
        );
    }
}

