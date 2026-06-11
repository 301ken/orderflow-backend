package com.example.orderflow.storage;

public interface DocumentStorageService {

    /**
     * Persists the content under the given key and returns a provider-specific
     * location reference (e.g. an S3 URI or a local file path).
     */
    String store(String key, byte[] content, String contentType);

    byte[] retrieve(String key);
}
