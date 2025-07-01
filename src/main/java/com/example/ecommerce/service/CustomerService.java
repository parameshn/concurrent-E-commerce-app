package com.example.ecommerce.service;

import org.springframework.stereotype.Service;
import com.example.ecommerce.entity.Customer;
import com.example.ecommerce.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;

import com.example.ecommerce.exception.CustomerAlreadyExistsException;
import com.example.ecommerce.exception.CustomerNotFoundException;


import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Service
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final ConcurrentHashMap<String, Customer> emailCache = new ConcurrentHashMap<>();

    @Transactional
    public Customer createCustomer(Customer customer) {
        rwLock.writeLock().lock();
        try {
            if (customerRepository.findByEmail(customer.getEmail()).isPresent())
                throw new CustomerAlreadyExistsException(customer.getEmail());
            Customer savedCustomer = customerRepository.save(customer);
            emailCache.put(savedCustomer.getEmail(), savedCustomer);
            return savedCustomer;
        } finally {
            rwLock.writeLock().unlock();
        }

    }
    
    public Optional<Customer> getCustomer(Long id) {
        rwLock.readLock().lock();
        try {
            return customerRepository.findById(id);
        } finally {
            rwLock.readLock().unlock();
        }
        /*
         * you should handle the Optional<Customer> in the controller layer, because:
         * 
         * The controller is responsible for translating business logic outcomes into
         * HTTP responses.
         * 
         * If a customer is not found, it's appropriate to return a 404 Not Found
         * response from the controller.
         * 
         */
    }
    
    public Optional<Customer> getCustomerByEmail(String email) {
        rwLock.readLock().lock();
        try {
            Customer cachedCustomer = emailCache.get(email);
            if (cachedCustomer != null) {
                return Optional.of(cachedCustomer);
            }
            Optional<Customer> customer = customerRepository.findByEmail(email);
            customer.ifPresent(c -> emailCache.put(email, c));
            return customer;
        } finally {
            rwLock.readLock().unlock();
        }
    }
    
    @Async
    public CompletableFuture<List<Customer>> getAllCustomerAsync() {
        return CompletableFuture.supplyAsync(() -> {
            rwLock.readLock().lock();
            try {
                return customerRepository.findAll();
            } finally {
                rwLock.readLock().unlock();
            }
        });

        /*
         * findAll() can be an expensive operation if there are many records.
         * 
         * Running it in a background thread improves responsiveness, especially in:
         * 
         * REST APIs that allow parallel aggregation
         * 
         * Dashboards with multiple data sources
         * 
         * Long-running UI refreshes or reports
         */
    }
    
    @Transactional
    public Customer updateCustomer(Long id, Customer updatedCustomer) {
        rwLock.writeLock().lock();
        try {
            Customer existingCustomer = customerRepository.findById(id)
                    .orElseThrow(() -> new CustomerNotFoundException(id));

            // Null safety (optional, based on whether fields are required or validated elsewhere)
            if (updatedCustomer.getEmail() == null || updatedCustomer.getFirstName() == null) {
                throw new IllegalArgumentException("Required fields cannot be null");
            }

            // Remove old email from cache if changing
            if (!existingCustomer.getEmail().equals(updatedCustomer.getEmail())) {
                emailCache.remove(existingCustomer.getEmail());
            }

            existingCustomer.setFirstName(updatedCustomer.getFirstName());
            existingCustomer.setLastName(updatedCustomer.getLastName());
            existingCustomer.setEmail(updatedCustomer.getEmail());
            existingCustomer.setAddress(updatedCustomer.getAddress());

            Customer savedCustomer = customerRepository.save(existingCustomer);
            emailCache.put(savedCustomer.getEmail(), savedCustomer);

            return savedCustomer;
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    @Transactional
    public boolean deletCustomer(Long id) {
        rwLock.writeLock().lock();
        try {
            Optional<Customer> customer = customerRepository.findById(id);
            if (customer.isPresent()) {
                emailCache.remove(customer.get().getEmail());
                customerRepository.deleteById(id);
                return true;
            }
            return false;
        } finally {
            rwLock.writeLock().unlock();

        }
    }
    
    public List<Customer> seatchCustomers(String searchTerm) {
        return customerRepository.findByFirstNameContainingOrLastNameContaining(searchTerm, searchTerm);

    }

}
/*
 * Service layer handles business rules. Controller layer handles HTTP
 * responses.
 * 
 * Method Type Where to handle "not found" Why?
 * createCustomer Service Layer Business logic violation (duplicate not allowed)
 * getCustomer Controller Layer Missing data is acceptable; how to present it is
 * an API decision
 */