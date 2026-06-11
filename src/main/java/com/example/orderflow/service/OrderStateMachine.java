package com.example.orderflow.service;

import com.example.orderflow.domain.OrderStatus;
import com.example.orderflow.exception.InvalidOrderStateTransitionException;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

@Component
public class OrderStateMachine {

    private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED = new EnumMap<>(OrderStatus.class);

    static {
        ALLOWED.put(OrderStatus.CREATED, Set.of(OrderStatus.PENDING_PAYMENT, OrderStatus.CANCELLED));
        ALLOWED.put(OrderStatus.PENDING_PAYMENT, Set.of(OrderStatus.PAID, OrderStatus.FAILED, OrderStatus.CANCELLED));
        ALLOWED.put(OrderStatus.PAID, Set.of(OrderStatus.PROCESSING, OrderStatus.CANCELLED));
        ALLOWED.put(OrderStatus.PROCESSING, Set.of(OrderStatus.COMPLETED, OrderStatus.FAILED));
        ALLOWED.put(OrderStatus.COMPLETED, Set.of());
        ALLOWED.put(OrderStatus.CANCELLED, Set.of());
        ALLOWED.put(OrderStatus.FAILED, Set.of());
    }

    public boolean canTransition(OrderStatus from, OrderStatus to) {
        return ALLOWED.getOrDefault(from, Set.of()).contains(to);
    }

    public void validateTransition(OrderStatus from, OrderStatus to) {
        if (from == to || !canTransition(from, to)) {
            throw new InvalidOrderStateTransitionException(from, to);
        }
    }
}
