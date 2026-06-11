package com.example.orderflow.domain;

public enum OrderStatus {
    CREATED,
    PENDING_PAYMENT,
    PAID,
    PROCESSING,
    COMPLETED,
    CANCELLED,
    FAILED
}
