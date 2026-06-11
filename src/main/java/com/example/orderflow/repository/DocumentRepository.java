package com.example.orderflow.repository;

import com.example.orderflow.domain.Document;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findByOrderId(Long orderId);
}
