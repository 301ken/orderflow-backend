package com.example.orderflow.exception;

import com.example.orderflow.domain.OrderStatus;

public class InvalidOrderStateTransitionException extends RuntimeException {

    public InvalidOrderStateTransitionException(OrderStatus from, OrderStatus to) {
        super("Illegal order status transition: " + from + " -> " + to);
    }
}
