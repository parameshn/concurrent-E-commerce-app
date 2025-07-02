package com.example.ecommerce.service;

import com.example.ecommerce.entity.Product;
import com.example.ecommerce.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.OptimisticLockException;

import com.example.ecommerce.exception.ProductNotFoundException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantReadWriteLock;



@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private Executor taskExecutor;

    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final AtomicLong operationCounter = new AtomicLong(0);
    private final ConcurrentHashMap<Long, Product> productCache = new ConcurrentHashMap<>();

    // CREATE - Thread-safe product creation
    @Transactional
    /*
     * @Transactional:
     * 
     * All operations in the method are treated as a single unit of work
     * 
     * If one operation fails, everything is rolled back
     */
    public Product createProduct(Product product) {
        rwLock.writeLock().lock();
        try {
            operationCounter.incrementAndGet();
            // You want to track how many products were created during the application's
            // runtime

            Product savedProduct = productRepository.save(product);
            productCache.put(savedProduct.getId(), savedProduct);
            return savedProduct;
        } finally {
            rwLock.writeLock().unlock();
            /*
             * Before returning the value, Java checks for a finally block.
             * 
             * If finally is present, it is guaranteed to run first, before the return
             * happens.
             * 
             * Once the finally block finishes, the actual return happens.
             */
        }
    }

    /*
     * For read-only operations, skipping @Transactional is often better (faster,
     * avoids unnecessary DB locking)
     */
    // READ - Thread-safe with caching and read locks
    @Transactional(readOnly = true)
    public Optional<Product> getProduct(Long id) {
        rwLock.readLock().lock();
        try {
            // check cache first
            Product cachedProduct = productCache.get(id);
            
            if (cachedProduct != null) {
                return Optional.of(cachedProduct);
            }

            Optional<Product> product = productRepository.findById(id);
            product.ifPresent(p -> productCache.put(id, p));
            return product;
        } finally {
            rwLock.readLock().unlock();
        }
    }

    // READ ALL - Concurrent processing
    @Async("taskExecutor")
    @Transactional(readOnly = true)
    public CompletableFuture<List<Product>> getAllProductsAsync() {
        return CompletableFuture.supplyAsync(() -> {
            rwLock.readLock().lock();
            try {
                return productRepository.findAll();
            } finally {
                rwLock.readLock().unlock();
            }
        }, taskExecutor);
    }

    // UPDATE - Optimistic locking with retry mechanism
    @Transactional
    public Product updateProduct(Long id, Product updatedProduct) {
        int maxRetries = 3;
        int retryCount = 0;

        while (retryCount < maxRetries) {
            try {
                rwLock.writeLock().lock(); // Optional if @Version is used properly
                try {
                    Optional<Product> existingProductOpt = productRepository.findById(id);
                    if (existingProductOpt.isEmpty()) {
                        throw new ProductNotFoundException(id);
                    }

                    Product existingProduct = existingProductOpt.get();

                    // Apply only non-null updates (safe partial update)
                    if (updatedProduct.getName() != null) {
                        existingProduct.setName(updatedProduct.getName());
                    }
                    if (updatedProduct.getDescription() != null) {
                        existingProduct.setDescription(updatedProduct.getDescription());
                    }
                    if (updatedProduct.getPrice() != null) {
                        existingProduct.setPrice(updatedProduct.getPrice());
                    }
                    if (updatedProduct.getStockQuantity() != null) {
                        existingProduct.setStockQuantity(updatedProduct.getStockQuantity());
                    }
                    if (updatedProduct.getCategory() != null) {
                        existingProduct.setCategory(updatedProduct.getCategory());
                    }

                    // Save with optimistic locking via @Version
                    Product savedProduct = productRepository.save(existingProduct);

                    // Update cache and counter
                    productCache.put(savedProduct.getId(), savedProduct);
                    operationCounter.incrementAndGet();

                    return savedProduct;
                } finally {
                    rwLock.writeLock().unlock();
                }

            } catch (OptimisticLockException e) {
                retryCount++;
                if (retryCount >= maxRetries) {
                    throw new RuntimeException("Failed to update product after " + maxRetries + " attempts", e);
                }

                try {
                    Thread.sleep(100 * retryCount); // Backoff strategy
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Update interrupted", ie);
                }
            }
        }

        throw new RuntimeException("Unexpected error during product update");
    }

    // DELETE - Thread-safe deletion
    @Transactional
    public boolean deleteProduct(Long id) {
        rwLock.writeLock().lock();
        try {
            if (!productRepository.existsById(id)) {
                // productRepository.deleteById(id);
                // productCache.remove(id);
                // operationCounter.incrementAndGet();
                // return true;
                throw new ProductNotFoundException(id);
            }
            productRepository.deleteById(id);
                productCache.remove(id);
                operationCounter.incrementAndGet();
                return true;
         } finally {
             rwLock.writeLock().unlock();
         }
    }

    // Thread-safe stock management with pessimistic locking
    @Transactional
    public boolean updateStock(Long productId, Integer quantityChange) {
        Optional<Product> productOpt = productRepository.findByIdForUpdate(productId);
        
        Product product = productOpt.orElseThrow(() -> new ProductNotFoundException(productId));
        /* internal representation
         * if (productOpt.isPresent()) {
         * return productOpt.get(); // Internally called, yes — but safely
         * } else {
         * throw new ProductNotFoundException(productId);
         * }
         */
        int newQuantity = product.getStockQuantity() + quantityChange;

        if (newQuantity < 0) {
            throw new RuntimeException("Insufficient stock. Available: " + product.getStockQuantity());
        }
        product.setStockQuantity(newQuantity);
        productRepository.save(product);
        productCache.put(productId, product);
        return true;
    }

    @Async("taskExecutor")
    public CompletableFuture<String> batchUpdatePrices(List<Long> productIds, Double priceMultiplier) {
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (Long productId : productIds) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    Optional<Product> productOpt = getProduct(productId);
                    if (productOpt.isPresent()) {
                        Product product = productOpt.get();
                        product.setPrice(product.getPrice().multiply(BigDecimal.valueOf(priceMultiplier)));
                        updateProduct(productId, product); // assumes proper locking
                    }
                } catch (Exception e) {
                    System.err.println("Failed to update price for product " + productId + ": " + e.getMessage());
                }
            }, taskExecutor); // Use Spring-managed executor
            futures.add(future);
        }

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> "Batch price update completed for " + productIds.size() + " products");
    }

    public List<Product> getProductsByCategory(String category) {
        return productRepository.findByCategory(category);
    }

    public List<Product> getLowStockProducts(Integer threshold) {
        return productRepository.findByStockQuantityLessThan(threshold);
    }

    public long getOperationCount() {
        return operationCounter.get();
    }

}

/*
 * typically, you increment such counters only for mutating operations like:
 * 
 * createProduct(...)
 * 
 * updateProduct(...)
 * 
 * deleteProduct(...)
 * 
 * Because these are side-effecting operations that change the state of the
 * system.
 */

 /*
  * Operation Locking Strategy Why This Choice?
  * updateProduct Optimistic Locking We assume conflicts are rare. If someone
  * else updates, the version check will fail at save() time and trigger a retry.
  * updateStock Pessimistic Locking Stock can change rapidly, and we cannot
  * afford race conditions (like overselling). So we lock the row immediately
  * when reading it.
  * 
  * Scenario Strategy
  * Editing a blog post Optimistic Locking (retry if someone else edits)
  * Selling a concert ticket Pessimistic Locking (lock immediately to avoid
  * double sale)
  */