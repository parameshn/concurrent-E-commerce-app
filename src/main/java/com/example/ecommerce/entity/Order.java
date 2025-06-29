package com.example.ecommerce.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id",nullable = false)
    private Customer customer;

    @Column(name = "order_date",nullable = false)
    private LocalDateTime orderDate;

    @Column(nullable = false,precision = 10,scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum OrderStatus {
        PENDING, PROCESSING, SHIPPED, DELIVERED, CANCELLED
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }


}
/*
 * You're inside the Order entity.
 * 
 * Order.customer is a single Customer object, not a collection.
 * 
 * So for each Order, the customer field points to one customer.
 * 
 * FetchType.LAZY means: only load that one customer from the DB when
 * getCustomer() is called.
 * 
 * Each customer can have many orders
 * 
 * And orders still have one customer
 * 
 * You can fetch orders lazily when needed via customer.getOrders()
 * 
 * With EAGER, every Order fetch also fetches a Customer even if you don't need
 * it. That’s wasteful if you’re loading 1000 orders.
 */