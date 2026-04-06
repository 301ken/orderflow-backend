package com.example.orderflow.service;

import com.example.orderflow.domain.Client;
import com.example.orderflow.domain.Order;
import com.example.orderflow.domain.OrderItem;
import com.example.orderflow.domain.Product;
import com.example.orderflow.exception.ProductNotFoundException;
import com.example.orderflow.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
public class OrderServiceReproductionTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductRepository productRepository;

    @Test
    public void testCreateOrderWithNullProductPrice() {
        // Save a product to ensure it exists
        Product product = new Product();
        product.setName("Reproduction Product");
        product.setPrice(10.0);
        product = productRepository.save(product);

        // Create an order with an item that only has the product ID (simulating a request body)
        Order order = new Order();
        
        Client client = new Client();
        client.setId(1L); // Mock client
        order.setClient(client);

        OrderItem item = new OrderItem();
        item.setQuantity(2);
        
        Product productRequest = new Product();
        productRequest.setId(product.getId());

        item.setProduct(productRequest);
        order.setItems(Collections.singletonList(item));

        // This should now succeed and calculate the total price correctly
        Order createdOrder = orderService.createOrder(order);
        
        assertEquals(20.0, createdOrder.getTotalPrice());
        assertEquals(1, createdOrder.getItems().size());
        OrderItem savedItem = createdOrder.getItems().get(0);
        assertEquals(10.0, savedItem.getUnitPrice());
        assertEquals(20.0, savedItem.getSubTotal());
    }

    @Test
    public void testCreateOrderWithNonExistentProduct() {
        Order order = new Order();
        Client client = new Client();
        client.setId(1L);
        order.setClient(client);

        OrderItem item = new OrderItem();
        item.setQuantity(1);
        
        Product productRequest = new Product();
        productRequest.setId(9999L); // Non-existent ID

        item.setProduct(productRequest);
        order.setItems(Collections.singletonList(item));

        assertThrows(ProductNotFoundException.class, () -> {
            orderService.createOrder(order);
        });
    }

    @Test
    public void testCreateOrderWithEmptyItems() {
        Order order = new Order();
        Client client = new Client();
        client.setId(1L);
        order.setClient(client);
        order.setItems(Collections.emptyList());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            orderService.createOrder(order);
        });
        assertEquals("Order must have at least one item", exception.getMessage());
    }

    @Test
    public void testCreateOrderWithMissingClient() {
        Order order = new Order();
        OrderItem item = new OrderItem();
        item.setQuantity(1);
        Product product = new Product();
        product.setId(1L);
        item.setProduct(product);
        order.setItems(Collections.singletonList(item));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            orderService.createOrder(order);
        });
        assertEquals("Order must have a client", exception.getMessage());
    }
}
