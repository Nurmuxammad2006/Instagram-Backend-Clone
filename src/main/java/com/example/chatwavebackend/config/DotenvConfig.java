package com.example.chatwavebackend.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.MapPropertySource;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class DotenvConfig implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        String[] paths = {
                "./",
                System.getProperty("user.dir") + "/",
                "/home/nurmuxammad/Documents/ChatWaveBackend/ChatWave/"
        };

        Dotenv dotenv = null;

        for (String path : paths) {
            File envFile = new File(path + ".env");
            if (envFile.exists()) {
                dotenv = Dotenv.configure()
                        .directory(path)
                        .filename(".env")
                        .ignoreIfMissing()
                        .load();
                break;
            }
        }

        if (dotenv == null) {
            System.out.println("❌ .env file NOT found!");
            return;
        }

        Map<String, Object> props = new HashMap<>();

        // Load only keys that are NOT system environment variables
        dotenv.entries().forEach(entry -> {
            String key = entry.getKey();
            String value = entry.getValue();

            // Only load if it's not already a system property
            if (System.getProperty(key) == null) {
                props.put(key, value);
            }
        });

        applicationContext.getEnvironment().getPropertySources()
                .addFirst(new MapPropertySource("dotenv", props));

        System.out.println("✅ Loaded " + props.size() + " properties from .env");
    }
}