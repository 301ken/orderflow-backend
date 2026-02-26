package com.example.orderflow.controller;

import com.example.orderflow.domain.Product;
import com.example.orderflow.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;



import java.util.List;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    //Get all products : 200 OK
    @GetMapping
    public ResponseEntity<List<Product>> getProducts() {
        return ResponseEntity.ok(productService.getProducts());
    }

    //Get product by ID : 200 OK, 404 if not found
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    //Add product : 201 Created
    @PostMapping
    public ResponseEntity<Product> addProduct(@Valid @RequestBody Product product) {
        Product saved = productService.addProduct(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    //Update product : 200 OK
    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id,
                                                 @Valid@RequestBody Product updatedProduct) {
        return ResponseEntity.ok(productService.updateProduct(id, updatedProduct));
    }

    //Delete product : 204 No Content
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}