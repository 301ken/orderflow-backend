package com.example.orderflow.domain;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Entity
@Data
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Instant timestamp;

    @Column(nullable = false)
    private String actor;

    @Column(nullable = false)
    private String action;

    private String entityType;

    private Long entityId;

    @Column(length = 1000)
    private String details;
}
