package com.example.documentservice.service.impl;

import com.example.documentservice.exception.StorageException;
import com.example.documentservice.service.S3Service;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3ServiceImpl implements S3Service {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Override
    @CircuitBreaker(name = "s3Service", fallbackMethod = "uploadFallback")
    @Retry(name = "s3Service")
    public String uploadFile(MultipartFile file, String key) {
        try {
            log.info("Uploading file to S3: {}", key);

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();

            s3Client.putObject(putObjectRequest,
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            log.info("File uploaded successfully: {}", key);
            return key;

        } catch (IOException e) {
            log.error("Failed to upload file to S3", e);
            throw new StorageException("Failed to upload file", e);
        }
    }

    @Override
    @CircuitBreaker(name = "s3Service")
    @Retry(name = "s3Service")
    public byte[] downloadFile(String key) {
        try {
            log.info("Downloading file from S3: {}", key);

            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            return s3Client.getObjectAsBytes(getObjectRequest).asByteArray();

        } catch (Exception e) {
            log.error("Failed to download file from S3", e);
            throw new StorageException("Failed to download file", e);
        }
    }

    @Override
    @CircuitBreaker(name = "s3Service")
    public void deleteFile(String key) {
        try {
            log.info("Deleting file from S3: {}", key);

            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
            log.info("File deleted successfully: {}", key);

        } catch (Exception e) {
            log.error("Failed to delete file from S3", e);
            throw new StorageException("Failed to delete file", e);
        }
    }

    @Override
    public String generateS3Key(String fileName) {
        String extension = fileName.substring(fileName.lastIndexOf("."));
        return String.format("%s/%s%s",
                UUID.randomUUID().toString().substring(0, 2),
                UUID.randomUUID().toString(),
                extension);
    }

    // Fallback method for circuit breaker
    public String uploadFallback(MultipartFile file, String key, Exception ex) {
        log.error("S3 upload failed, using fallback", ex);
        // Could implement local storage fallback or queue for retry
        throw new StorageException("S3 service temporarily unavailable", ex);
    }
}
