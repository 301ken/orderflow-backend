package com.example.orderflow.payment;

public record PaymentEventResult(Long orderId, PaymentOutcome outcome) {

    public enum PaymentOutcome {
        SUCCEEDED,
        FAILED,
        IGNORED
    }
}
