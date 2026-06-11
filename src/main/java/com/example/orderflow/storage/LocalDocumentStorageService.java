package com.example.orderflow.storage;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Local filesystem fallback used when S3 is disabled, so document upload/download
 * works without AWS credentials.
 */
@Service
@Slf4j
@ConditionalOnProperty(name = "aws.s3.enabled", havingValue = "false", matchIfMissing = true)
public class LocalDocumentStorageService implements DocumentStorageService {

    @Value("${storage.local.base-dir:${java.io.tmpdir}/orderflow-docs}")
    private String baseDir;

    @PostConstruct
    void init() throws IOException {
        Files.createDirectories(Paths.get(baseDir));
        log.info("[local-storage] document base dir {}", baseDir);
    }

    @Override
    public String store(String key, byte[] content, String contentType) {
        try {
            Path target = resolve(key);
            Files.createDirectories(target.getParent());
            Files.write(target, content);
            return target.toString();
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to store document " + key, e);
        }
    }

    @Override
    public byte[] retrieve(String key) {
        try {
            return Files.readAllBytes(resolve(key));
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read document " + key, e);
        }
    }

    private Path resolve(String key) {
        return Paths.get(baseDir).resolve(key).normalize();
    }
}
