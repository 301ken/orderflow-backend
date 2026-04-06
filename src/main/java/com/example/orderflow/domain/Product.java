package com.example.orderflow.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Product name cannot be empty")
    private String name;

    @NotNull(message = "Price cannot be null")
    @Positive(message = "Price must be greater than zero")
    private Double price;
}