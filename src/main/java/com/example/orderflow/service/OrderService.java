package com.example.orderflow.service;

import com.example.orderflow.domain.Order;
import com.example.orderflow.domain.OrderItem;
import com.example.orderflow.domain.Product;
import com.example.orderflow.repository.OrderRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductService productService;

    @Transactional
    public Order createOrder(Order order) {
        if (order.getItems() == null || order.getItems().isEmpty()) {
            throw new RuntimeException("Order must have at least one item");
        }

        if (order.getClient() == null || order.getClient().getId() == null) {
            throw new RuntimeException("Order must have a client");
        }

        double total = 0;

        for (OrderItem item : order.getItems()) {
            item.setOrder(order);
            Product product = productService.getProductById(item.getProduct().getId());
            item.setProduct(product);
            item.setUnitPrice(product.getPrice());
            item.setSubTotal(product.getPrice() * item.getQuantity());
            total += item.getSubTotal();
        }

        order.setTotalPrice(total);

        return orderRepository.save(order);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));
    }

    public List<Order> getOrdersByClientId(Long clientId) {
        return orderRepository.findByClientId(clientId);
    }
}