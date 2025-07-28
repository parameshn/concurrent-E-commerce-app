package com.example.ecommerce.service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionService;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.concurrent.ExecutorCompletionService;
import java.util.*;
import jakarta.annotation.PreDestroy;

@Service
public class BatchProcessingService {
    @Autowired
    private ProductService productService;

    private final ExecutorService batchExecutor = Executors.newFixedThreadPool(4);
    private final CompletionService<String> completionService = new ExecutorCompletionService<>(batchExecutor);

    public CompletableFuture<String> processBatchOperations(List<Runnable> operations) {
        /*
         * It lets you batch up many small tasks and then dispatch them all through a
         * thread pool (or via your BatchProcessingService).
         */
        return CompletableFuture.supplyAsync(() -> {
            CountDownLatch latch = new CountDownLatch(operations.size());
            AtomicInteger successCount = new AtomicInteger(0);
            AtomicInteger errorCount = new AtomicInteger(0);

            for (Runnable opertion : operations) {
                batchExecutor.submit(() -> {
                    try {
                        opertion.run(); // The pool (size = 4) will pick up up to four tasks right away; the rest pile
                                        // up in an unbounded queue.

                        successCount.incrementAndGet();
                    } catch (Exception e) {
                        errorCount.incrementAndGet();
                        System.err.println("Batch opeartion failed: " + e.getMessage());
                    } finally {
                        latch.countDown();
                    }
                });

            }
            try {
                latch.await(30, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return "Batch processing interrupted";
            }

            return String.format("Batch processing completed. Success: %d, Errors: %d",
                    successCount.get(), errorCount.get());
        });
    }

    @PreDestroy
    public void cleanup() {
        batchExecutor.shutdown();
        try {
            if (!batchExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                batchExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            batchExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
