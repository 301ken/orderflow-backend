package com.example.orderflow.controller;

import com.example.orderflow.domain.Order;
import com.example.orderflow.domain.OrderStatus;
import com.example.orderflow.payment.PaymentEventResult;
import com.example.orderflow.payment.PaymentResult;
import com.example.orderflow.payment.PaymentService;
import com.example.orderflow.service.AuditService;
import com.example.orderflow.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class PaymentController {

    private final OrderService orderService;
    private final PaymentService paymentService;
    private final AuditService auditService;

    @PostMapping("/orders/{id}/pay")
    public ResponseEntity<PaymentResult> pay(@PathVariable Long id) {
        Order order = orderService.getOrderById(id);
        PaymentResult result = paymentService.createPaymentIntent(order);
        orderService.changeStatus(id, OrderStatus.PENDING_PAYMENT);
        auditService.record("PAYMENT_INITIATED", "Order", id,
                "provider=" + result.provider() + ", intent=" + result.paymentIntentId());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/webhooks/stripe")
    public ResponseEntity<String> stripeWebhook(
            @RequestBody String payload,
            @RequestHeader(value = "Stripe-Signature", required = false) String signature) {

        PaymentEventResult event = paymentService.handleWebhook(payload, signature);
        if (event.orderId() == null || event.outcome() == PaymentEventResult.PaymentOutcome.IGNORED) {
            return ResponseEntity.ok("ignored");
        }

        OrderStatus target = event.outcome() == PaymentEventResult.PaymentOutcome.SUCCEEDED
                ? OrderStatus.PAID
                : OrderStatus.FAILED;
        orderService.changeStatus(event.orderId(), target);
        auditService.record("PAYMENT_WEBHOOK", "Order", event.orderId(), "outcome=" + event.outcome());
        return ResponseEntity.ok("processed");
    }
}
