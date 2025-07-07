package com.example.ecommerce.controller;

import org.springframework.http.ResponseEntity;

import com.example.ecommerce.entity.Customer;
import com.example.ecommerce.service.CustomerService;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;


import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.*;


import java.util.List;

import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/customer")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @PostMapping
    public ResponseEntity<Customer> createCustomer(@RequestBody Customer customer) {
        Customer savedCustomer = customerService.createCustomer(customer);
        return ResponseEntity.ok(savedCustomer);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Customer> getCustomer(@PathVariable Long id) {
        Optional<Customer> customer = customerService.getCustomer(id);
        return customer.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<Customer> getCustomerByEmail(@PathVariable String email) {
        Optional<Customer> customer = customerService.getCustomerByEmail(email);
        return customer.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping()
    public ResponseEntity<List<Customer>> getAllCustomer() throws ExecutionException, InterruptedException {
        CompletableFuture<List<Customer>> futureCustomers = customerService.getAllCustomerAsync();
        List<Customer> customers = futureCustomers.get();
        return ResponseEntity.ok(customers);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Customer> updateCustomer(@PathVariable Long id, @RequestBody Customer customer) {
        try {
            Customer updatedCustomer = customerService.updateCustomer(id, customer);
            return ResponseEntity.ok(updatedCustomer);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletedCustomer(@PathVariable Long id) {
        boolean deletedCustomer = customerService.deleteCustomer(id);
        return deletedCustomer ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();

    }

    @GetMapping({"/search"})
    public ResponseEntity<List<Customer>> searchCustomers(@RequestParam String term) {
        List<Customer> customers = customerService.searchCustomers(term);
        return ResponseEntity.ok(customers);
    }
}
