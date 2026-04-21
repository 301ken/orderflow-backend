package com.example.orderflow.auth.dto;

public record LoginResponse(String token, long expiresInSeconds) {
}
