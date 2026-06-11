package com.example.orderflow.payment;

import com.example.orderflow.domain.Order;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.StripeObject;
import com.stripe.net.Webhook;
import com.stripe.param.PaymentIntentCreateParams;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

/**
 * Real Stripe integration, activated only when {@code stripe.enabled=true} and a
 * valid API key is supplied. When disabled, {@link NoopPaymentService} is used
 * instead so the application still builds and runs without credentials.
 */
@Service
@Slf4j
@ConditionalOnProperty(name = "stripe.enabled", havingValue = "true")
public class StripePaymentService implements PaymentService {

    @Value("${stripe.api-key:}")
    private String apiKey;

    @Value("${stripe.webhook-secret:}")
    private String webhookSecret;

    @Value("${stripe.currency:usd}")
    private String currency;

    @PostConstruct
    void init() {
        Stripe.apiKey = apiKey;
        log.info("[stripe] payment service initialized");
    }

    @Override
    public PaymentResult createPaymentIntent(Order order) {
        long amountInCents = Math.round(order.getTotalPrice() * 100);
        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amountInCents)
                .setCurrency(currency)
                .putMetadata("orderId", String.valueOf(order.getId()))
                .build();
        try {
            PaymentIntent intent = PaymentIntent.create(params);
            return new PaymentResult("stripe", intent.getId(), intent.getClientSecret(), intent.getStatus());
        } catch (StripeException e) {
            throw new IllegalStateException("Stripe payment intent creation failed: " + e.getMessage(), e);
        }
    }

    @Override
    public PaymentEventResult handleWebhook(String payload, String signatureHeader) {
        Event event;
        try {
            event = Webhook.constructEvent(payload, signatureHeader, webhookSecret);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid Stripe webhook signature: " + e.getMessage(), e);
        }

        Optional<StripeObject> deserialized = event.getDataObjectDeserializer().getObject();
        if (deserialized.isEmpty() || !(deserialized.get() instanceof PaymentIntent intent)) {
            return new PaymentEventResult(null, PaymentEventResult.PaymentOutcome.IGNORED);
        }

        Long orderId = extractOrderId(intent.getMetadata());
        return switch (event.getType()) {
            case "payment_intent.succeeded" ->
                    new PaymentEventResult(orderId, PaymentEventResult.PaymentOutcome.SUCCEEDED);
            case "payment_intent.payment_failed" ->
                    new PaymentEventResult(orderId, PaymentEventResult.PaymentOutcome.FAILED);
            default -> new PaymentEventResult(orderId, PaymentEventResult.PaymentOutcome.IGNORED);
        };
    }

    private Long extractOrderId(Map<String, String> metadata) {
        if (metadata == null || metadata.get("orderId") == null) {
            return null;
        }
        try {
            return Long.parseLong(metadata.get("orderId"));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
