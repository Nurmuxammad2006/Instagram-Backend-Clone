package com.example.chatwavebackend.storage;

import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;
import software.amazon.awssdk.core.sync.RequestBody;

import java.io.IOException;
import java.time.Duration;
import java.util.UUID;

@Service
public class S3Service {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${aws.s3.region}")
    private String region;

    private final String effectiveRegion;

    public S3Service(S3Client s3Client) {
        this.s3Client = s3Client;

        // Safe region handling with fallback
        this.effectiveRegion = (region != null && !region.trim().isEmpty()) ? region : "us-east-1";

        this.s3Presigner = S3Presigner.builder()
                .region(Region.of(effectiveRegion))
                .build();
    }

    // Upload file directly to S3
    public String uploadFile(MultipartFile file, String folder) throws IOException {
        String fileName = generateFileName(folder, file.getOriginalFilename());
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .contentType(file.getContentType())
                .build();
        s3Client.putObject(request, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        return getFileUrl(fileName);
    }

    // Delete file from S3
    public void deleteFile(String fileUrl) {
        String key = extractKeyFromUrl(fileUrl);
        DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();
        s3Client.deleteObject(request);
    }

    // Generate presigned URL for frontend direct upload
    public String generatePresignedUrl(String fileName, String folder, int expirationMinutes) {
        String fullPath = folder + "/" + fileName;
        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(expirationMinutes))
                .putObjectRequest(req -> req.bucket(bucketName).key(fullPath))
                .build();
        return s3Presigner.presignPutObject(presignRequest).url().toString();
    }

    // Get public URL of file
    public String getFileUrl(String fileKey) {
        return String.format("https://%s.s3.%s.amazonaws.com/%s",
                bucketName, effectiveRegion, fileKey);
    }

    // Generate unique filename
    private String generateFileName(String folder, String originalFilename) {
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        return folder + "/" + UUID.randomUUID() + extension;
    }

    // Extract key from full URL
    private String extractKeyFromUrl(String fileUrl) {
        String baseUrl = String.format("https://%s.s3.%s.amazonaws.com/", bucketName, effectiveRegion);
        return fileUrl.replace(baseUrl, "");
    }

    @PreDestroy
    public void shutdown() {
        if (s3Presigner != null) {
            s3Presigner.close();
        }
    }
}