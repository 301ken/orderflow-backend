package com.example.orderflow.payment;

import com.example.orderflow.domain.Order;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * Local fallback used when Stripe is disabled. Lets the app boot, run, and be
 * tested without live Stripe credentials. The webhook accepts a simplified
 * payload: {"orderId": <id>, "outcome": "SUCCEEDED|FAILED"}.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "stripe.enabled", havingValue = "false", matchIfMissing = true)
public class NoopPaymentService implements PaymentService {

    private final ObjectMapper objectMapper;

    @Override
    public PaymentResult createPaymentIntent(Order order) {
        String intentId = "pi_local_" + order.getId();
        log.info("[noop-payment] created stub payment intent {} for order {} amount {}",
                intentId, order.getId(), order.getTotalPrice());
        return new PaymentResult("noop", intentId, intentId + "_secret", "requires_payment_method");
    }

    @Override
    public PaymentEventResult handleWebhook(String payload, String signatureHeader) {
        try {
            JsonNode node = objectMapper.readTree(payload);
            Long orderId = node.path("orderId").asLong();
            String outcome = node.path("outcome").asText("IGNORED");
            return new PaymentEventResult(orderId, PaymentEventResult.PaymentOutcome.valueOf(outcome));
        } catch (Exception e) {
            log.warn("[noop-payment] could not parse webhook payload: {}", e.getMessage());
            return new PaymentEventResult(null, PaymentEventResult.PaymentOutcome.IGNORED);
        }
    }
}
