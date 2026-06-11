package com.example.orderflow.payment;

import com.example.orderflow.domain.Order;

public interface PaymentService {

    PaymentResult createPaymentIntent(Order order);

    PaymentEventResult handleWebhook(String payload, String signatureHeader);
}
