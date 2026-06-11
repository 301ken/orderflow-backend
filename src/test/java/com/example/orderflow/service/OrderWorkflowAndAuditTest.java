package com.example.orderflow.service;

import com.example.orderflow.domain.Client;
import com.example.orderflow.domain.Order;
import com.example.orderflow.domain.OrderItem;
import com.example.orderflow.domain.OrderStatus;
import com.example.orderflow.domain.Product;
import com.example.orderflow.exception.InvalidOrderStateTransitionException;
import com.example.orderflow.repository.AuditLogRepository;
import com.example.orderflow.repository.ClientRepository;
import com.example.orderflow.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class OrderWorkflowAndAuditTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    private Order persistOrder() {
        Client client = new Client();
        client.setName("Workflow Client");
        client.setEmail("workflow@example.com");
        client = clientRepository.save(client);

        Product product = new Product();
        product.setName("Workflow Product");
        product.setPrice(15.0);
        product = productRepository.save(product);

        Order order = new Order();
        order.setClient(client);
        OrderItem item = new OrderItem();
        item.setQuantity(2);
        Product ref = new Product();
        ref.setId(product.getId());
        item.setProduct(ref);
        order.setItems(Collections.singletonList(item));

        return orderService.createOrder(order);
    }

    @Test
    void newOrderStartsInCreatedAndWritesAuditLog() {
        long before = auditLogRepository.count();
        Order created = persistOrder();

        assertEquals(OrderStatus.CREATED, created.getStatus());
        assertTrue(auditLogRepository.count() > before);
        assertTrue(auditLogRepository.findByEntityTypeAndEntityIdOrderByTimestampDesc("Order", created.getId())
                .stream().anyMatch(log -> log.getAction().equals("ORDER_CREATED")));
    }

    @Test
    void validTransitionUpdatesStatusAndAudits() {
        Order created = persistOrder();
        Order updated = orderService.changeStatus(created.getId(), OrderStatus.PENDING_PAYMENT);

        assertEquals(OrderStatus.PENDING_PAYMENT, updated.getStatus());
        assertTrue(auditLogRepository.findByEntityTypeAndEntityIdOrderByTimestampDesc("Order", created.getId())
                .stream().anyMatch(log -> log.getAction().equals("ORDER_STATUS_CHANGED")));
    }

    @Test
    void invalidTransitionIsRejected() {
        Order created = persistOrder();
        assertThrows(InvalidOrderStateTransitionException.class,
                () -> orderService.changeStatus(created.getId(), OrderStatus.COMPLETED));
    }
}
