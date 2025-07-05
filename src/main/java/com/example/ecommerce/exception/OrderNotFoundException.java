package com.example.ecommerce.exception;




public class OrderNotFoundException extends RuntimeException {

    public OrderNotFoundException(Long id) {
        super("Order not found with id:" + id);
    }
}
