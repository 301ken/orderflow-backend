package com.example.orderflow.service;

import com.example.orderflow.domain.Document;
import com.example.orderflow.repository.DocumentRepository;
import com.example.orderflow.storage.DocumentStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentStorageService storageService;
    private final OrderService orderService;
    private final AuditService auditService;

    public Document upload(Long orderId, MultipartFile file) {
        orderService.getOrderById(orderId);

        String original = file.getOriginalFilename() == null ? "document" : file.getOriginalFilename();
        String key = "orders/" + orderId + "/" + UUID.randomUUID() + "-" + original;

        byte[] content;
        try {
            content = file.getBytes();
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read uploaded file", e);
        }

        storageService.store(key, content, file.getContentType());

        Document document = new Document();
        document.setOrderId(orderId);
        document.setFilename(original);
        document.setStorageKey(key);
        document.setContentType(file.getContentType());
        document.setSize(file.getSize());
        document.setUploadedAt(Instant.now());

        Document saved = documentRepository.save(document);
        auditService.record("DOCUMENT_UPLOADED", "Order", orderId, "documentId=" + saved.getId() + ", key=" + key);
        return saved;
    }

    public List<Document> listByOrder(Long orderId) {
        return documentRepository.findByOrderId(orderId);
    }
}
