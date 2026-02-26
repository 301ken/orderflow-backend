package com.example.orderflow.service;

import com.example.orderflow.domain.Product;
import com.example.orderflow.exception.ProductNotFoundException;
import com.example.orderflow.repository.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


import java.util.List;


@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;


    //Get all products
    public List<Product> getProducts() {
        return productRepository.findAll();
    }

    //Get product by ID, throw 404 if not found
    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    //Add product
    public Product addProduct(Product product) {
        return productRepository.save(product);
    }

    //Update product
    @Transactional
    public Product updateProduct(Long id, Product updatedProduct) {
        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        existing.setName(updatedProduct.getName());
        existing.setPrice(updatedProduct.getPrice());
        return existing;
    }

    //Delete product
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ProductNotFoundException(id);
        }
        productRepository.deleteById(id);
    }
}