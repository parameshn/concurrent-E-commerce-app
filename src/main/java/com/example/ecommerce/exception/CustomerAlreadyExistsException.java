package com.example.ecommerce.exception;



public class CustomerAlreadyExistsException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public CustomerAlreadyExistsException(String email) {
        super("customer with email " + email + " already exists");
    }
}
