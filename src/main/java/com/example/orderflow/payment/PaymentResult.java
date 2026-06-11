package com.example.orderflow.payment;

public record PaymentResult(
        String provider,
        String paymentIntentId,
        String clientSecret,
        String status
) {
}
