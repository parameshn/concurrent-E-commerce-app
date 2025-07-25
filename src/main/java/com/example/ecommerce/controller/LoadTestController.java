package com.example.ecommerce.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.example.ecommerce.service.AnalyticsService;
import com.example.ecommerce.service.CacheService;
import com.example.ecommerce.service.BatchProcessingService;
import com.example.ecommerce.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import java.util.concurrent.CompletableFuture;


import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/load-test")
public class LoadTestController {

    @Autowired
    private ProductService productService;

    @Autowired 
    private BatchProcessingService batchProcessingService;


    @PostMapping("/concurrent-reads/{threads}/{iterations}")
    public ResponseEntity<String> testConcurrentReads(@PathVariable int threads, @PathVariable int iterations)
            throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threads);
        AtomicLong successCount = new AtomicLong(0);

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < threads; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    startLatch.await();
                    for (int j = 0; j < iterations; j++) {
                        productService.getProduct((long) (threadId % 10 + 1));
                        successCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    System.err.println("Read test error:" + e.getMessage());
                } finally {
                    endLatch.countDown();
                }
            });

        }
        startLatch.countDown();
        endLatch.await();
        long endTime = System.currentTimeMillis();

        executor.shutdown();

        return ResponseEntity.ok(String.format(
                "Concurrent read test completed: %d threads, %d iterations each. " +
                        "Successful reads: %d, Time: %d ms, Avg per operation: %.2f ms",
                threads, iterations, successCount.get(), endTime - startTime,
                (double) (endTime - startTime) / successCount.get()));
    }
    
    @PostMapping("/batch-operations/{count}")
    public ResponseEntity<String> testBatchOperations(@PathVariable int count)
            throws ExecutionException, InterruptedException {
        List<Runnable> operations = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            final int index = i;
            operations.add(() -> {
                try {
                    productService.getProduct((long) (index % 10 + 1));
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        CompletableFuture<String> result = batchProcessingService.processBatchOperations(operations);
        return ResponseEntity.ok(result.get());


    }

}
