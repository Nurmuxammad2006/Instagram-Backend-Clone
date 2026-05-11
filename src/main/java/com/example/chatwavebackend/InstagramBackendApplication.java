package com.example.chatwavebackend;

import com.example.chatwavebackend.config.DotenvConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class InstagramBackendApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(InstagramBackendApplication.class);

        // Load .env file before anything else
        app.addInitializers(new DotenvConfig());

        app.run(args);
    }
}