package com.example.orderflow.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    //Product not found : 404
    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<Object> handleProductNotFound(ProductNotFoundException ex) {
        return errorBody(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage());
    }

    //Illegal order status transition : 400
    @ExceptionHandler(InvalidOrderStateTransitionException.class)
    public ResponseEntity<Object> handleInvalidTransition(InvalidOrderStateTransitionException ex) {
        return errorBody(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage());
    }

    //Bad argument from integrations / requests : 400
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgument(IllegalArgumentException ex) {
        return errorBody(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage());
    }

    //Authorization failure : 403
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> handleAccessDenied(AccessDeniedException ex) {
        return errorBody(HttpStatus.FORBIDDEN, "Forbidden", "Access denied");
    }

    //Input validation errors : 400
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    //Generic exception : 500
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGenericException(Exception ex) {
        String message = ex.getMessage();
        if (ex instanceof java.util.NoSuchElementException || (message != null && message.contains("not found"))) {
            return errorBody(HttpStatus.NOT_FOUND, "Not Found", message);
        }
        return errorBody(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", message);
    }

    private ResponseEntity<Object> errorBody(HttpStatus status, String error, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", error);
        body.put("message", message);
        return new ResponseEntity<>(body, status);
    }
}