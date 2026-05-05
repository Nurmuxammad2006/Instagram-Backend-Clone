package com.example.chatwavebackend.storage.dto;

import java.util.Date;

public class PresignedUrlResponse {
    private final String url;
    private final String filePath;
    private final Date expiresAt;

    public PresignedUrlResponse(String url, String filePath, Date expiresAt) {
        this.url = url;
        this.filePath = filePath;
        this.expiresAt = expiresAt;
    }

    // Getters
    public String getUrl() { return url; }
    public String getFilePath() { return filePath; }
    public Date getExpiresAt() { return expiresAt; }
}