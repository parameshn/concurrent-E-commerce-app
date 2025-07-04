package com.example.ecommerce.service;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.example.ecommerce.repository.OrderRepository;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.transaction.Transactional;

import com.example.ecommerce.entity.Order;
import java.util.List;
import java.util.Optional;

import com.example.ecommerce.exception.OrderNotFoundException;

import java.util.concurrent.Executor;

@Service
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    @Qualifier("taskExecutor")
    private Executor taskExecutor;

    private final BlockingQueue<Order> orderProcessingQueue = new ArrayBlockingQueue<>(1000);
    private final ExecutorService orderProcessor = Executors.newFixedThreadPool(3);
    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();

    @PostConstruct
    public void initOrderProcessor() {
        for (int i = 0; i < 3; i++) {
            orderProcessor.submit(this::processOrdersFromQueue);
        }
    }

    @Transactional
    public Order createOrder(Order order) {
        rwLock.writeLock().lock();
        try {
            Order savedOrder = orderRepository.save(order);
            try {
                boolean added = orderProcessingQueue.offer(savedOrder, 5, TimeUnit.SECONDS);
                /*
                 * You're using:
                 * 
                 * BlockingQueue<Order> as a thread-safe buffer
                 * 
                 * offer(...) to produce (enqueue) an order
                 * 
                 * take() in a separate thread to consume (process) the order
                 */
                if (!added) {
                    throw new RuntimeException("Failed to queue order for processing: queue full");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Failed to queue order for processing", e);
            }
            return savedOrder;

        } finally {
            rwLock.writeLock().unlock();
        }
    }

    // Producer-Consumer pattern for order processing
    private void processOrdersFromQueue() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Order order = orderProcessingQueue.take();
                processOrder(order);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // restore interrupt
                break; // exit the loop gracefully
            }
        }
    }

    @Transactional
    public Order updateOrderStatus(Long id, Order.OrderStatus newStatus) {
        rwLock.writeLock().lock();
        try {
            Optional<Order> orderOpt = orderRepository.findById(id);
            if (orderOpt.isEmpty()) {
                throw new RuntimeException("Order not found with id:" + id);
            }

            Order order = orderOpt.get();
            order.setStatus(newStatus);
            return orderRepository.save(order);
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    public Optional<Order> getOrder(Long id) {
        rwLock.readLock().lock();
        try {
            return orderRepository.findById(id);
        } finally {
            rwLock.readLock().unlock();
        }
    }

    @Async
    public CompletableFuture<List<Order>> getAllOrdersAsync() {
        return CompletableFuture.supplyAsync(() -> {
            rwLock.readLock().lock();
            try {
                return orderRepository.findAll();
            } finally {
                rwLock.readLock().unlock();
            }
        }, taskExecutor);
    }

    @Async
    public void processOrder(Order order) {
        try {
            Thread.sleep(2000);
            updateOrderStatus(order.getId(), Order.OrderStatus.PROCESSING);
            Thread.sleep(3000);
            updateOrderStatus(order.getId(), Order.OrderStatus.SHIPPED);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Transactional
    public boolean deleteOrder(Long id) {
        rwLock.writeLock().lock();
        try {
            if (!orderRepository.existsById(id)) {
                throw new OrderNotFoundException(id);
            }
            orderRepository.deleteById(id);
            return true;
        } finally {
            rwLock.writeLock().unlock();
        }

    }

    public List<Order> getOrdersByCustomer(Long customerId) {
        return orderRepository.findByCustomerId(customerId);
    }

    public List<Order> getOrdersByStatus(Order.OrderStatus status) {
        return orderRepository.findByStatus(status);
    }

    @PreDestroy
    public void shutdownOrderProcessor() {
        orderProcessor.shutdownNow();
    }

    public int getQueueSize() {
        return orderProcessingQueue.size();
    }

}
