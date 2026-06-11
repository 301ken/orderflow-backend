package com.example.orderflow.service;

import com.example.orderflow.domain.OrderStatus;
import com.example.orderflow.exception.InvalidOrderStateTransitionException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderStateMachineTest {

    private final OrderStateMachine stateMachine = new OrderStateMachine();

    @Test
    void allowsHappyPathTransitions() {
        assertTrue(stateMachine.canTransition(OrderStatus.CREATED, OrderStatus.PENDING_PAYMENT));
        assertTrue(stateMachine.canTransition(OrderStatus.PENDING_PAYMENT, OrderStatus.PAID));
        assertTrue(stateMachine.canTransition(OrderStatus.PAID, OrderStatus.PROCESSING));
        assertTrue(stateMachine.canTransition(OrderStatus.PROCESSING, OrderStatus.COMPLETED));
    }

    @Test
    void rejectsIllegalTransitions() {
        assertFalse(stateMachine.canTransition(OrderStatus.CREATED, OrderStatus.PAID));
        assertFalse(stateMachine.canTransition(OrderStatus.COMPLETED, OrderStatus.PROCESSING));
        assertThrows(InvalidOrderStateTransitionException.class,
                () -> stateMachine.validateTransition(OrderStatus.CREATED, OrderStatus.COMPLETED));
    }

    @Test
    void rejectsNoOpTransition() {
        assertThrows(InvalidOrderStateTransitionException.class,
                () -> stateMachine.validateTransition(OrderStatus.CREATED, OrderStatus.CREATED));
    }

    @Test
    void terminalStatesHaveNoOutgoingTransitions() {
        assertDoesNotThrow(() -> stateMachine.canTransition(OrderStatus.CANCELLED, OrderStatus.PAID));
        assertFalse(stateMachine.canTransition(OrderStatus.CANCELLED, OrderStatus.PAID));
        assertFalse(stateMachine.canTransition(OrderStatus.FAILED, OrderStatus.PROCESSING));
    }
}
