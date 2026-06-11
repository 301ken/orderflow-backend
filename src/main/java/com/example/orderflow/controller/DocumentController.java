package com.example.orderflow.controller;

import com.example.orderflow.domain.Document;
import com.example.orderflow.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/orders/{orderId}/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping
    public ResponseEntity<Document> upload(@PathVariable Long orderId,
                                           @RequestParam("file") MultipartFile file) {
        Document saved = documentService.upload(orderId, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping
    public ResponseEntity<List<Document>> list(@PathVariable Long orderId) {
        return ResponseEntity.ok(documentService.listByOrder(orderId));
    }
}
