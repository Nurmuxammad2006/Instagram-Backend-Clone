package com.example.instagrambackend.service;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitService {

    // Separate buckets for login and send-code per IP
    private final Map<String, Bucket> loginBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> sendCodeBuckets = new ConcurrentHashMap<>();

    // 10 requests per 1 minute
    private Bucket createLoginBucket() {
        return Bucket.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(10)
                        .refillIntervally(10, Duration.ofMinutes(1))
                        .build())
                .build();
    }

    // 3 requests per 10 minutes
    private Bucket createSendCodeBucket() {
        return Bucket.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(3)
                        .refillIntervally(3, Duration.ofMinutes(10))
                        .build())
                .build();
    }

    public boolean tryLoginConsume(String ip) {
        Bucket bucket = loginBuckets.computeIfAbsent(ip, k -> createLoginBucket());
        return bucket.tryConsume(1);
    }

    public boolean trySendCodeConsume(String ip) {
        Bucket bucket = sendCodeBuckets.computeIfAbsent(ip, k -> createSendCodeBucket());
        return bucket.tryConsume(1);
    }
}
