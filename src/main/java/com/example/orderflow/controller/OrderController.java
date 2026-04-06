package com.example.orderflow.controller;

import com.example.orderflow.domain.Order;
import com.example.orderflow.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<Order> create(@RequestBody Order order) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(orderService.createOrder(order));
    }

    @GetMapping
    public ResponseEntity<List<Order>> getAll(@RequestParam(required = false) Long clientId) {
        if (clientId != null) {
            return ResponseEntity.ok(orderService.getOrdersByClientId(clientId));
        }
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }
}