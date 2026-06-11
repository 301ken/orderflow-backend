package com.example.orderflow.domain;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Entity
@Data
@Table(name = "documents")
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long orderId;

    @Column(nullable = false)
    private String filename;

    @Column(nullable = false)
    private String storageKey;

    private String contentType;

    private long size;

    @Column(nullable = false)
    private Instant uploadedAt;
}
