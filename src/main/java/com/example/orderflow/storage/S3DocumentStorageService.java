package com.example.orderflow.storage;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

/**
 * Real AWS S3 integration, activated only when {@code aws.s3.enabled=true}.
 * Credentials are resolved through the default AWS provider chain. When disabled,
 * {@link LocalDocumentStorageService} is used instead.
 */
@Service
@Slf4j
@ConditionalOnProperty(name = "aws.s3.enabled", havingValue = "true")
public class S3DocumentStorageService implements DocumentStorageService {

    @Value("${aws.s3.bucket:}")
    private String bucket;

    @Value("${aws.region:us-east-1}")
    private String region;

    private S3Client s3Client;

    @PostConstruct
    void init() {
        this.s3Client = S3Client.builder()
                .region(Region.of(region))
                .build();
        log.info("[s3-storage] initialized bucket={} region={}", bucket, region);
    }

    @PreDestroy
    void shutdown() {
        if (s3Client != null) {
            s3Client.close();
        }
    }

    @Override
    public String store(String key, byte[] content, String contentType) {
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType)
                .build();
        s3Client.putObject(request, RequestBody.fromBytes(content));
        return "s3://" + bucket + "/" + key;
    }

    @Override
    public byte[] retrieve(String key) {
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();
        return s3Client.getObjectAsBytes(request).asByteArray();
    }
}
