package com.example.ecommerce.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;



import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.StampedLock;
import java.util.stream.Collectors;

public class AnalyticsService {

   // @Autowired
  //  private ProductService productService;

    @Autowired
    private OrderService orderService;


    private final StampedLock stampedLock = new StampedLock();
    private final AtomicLong totalRevenue = new AtomicLong();
    private final ConcurrentHashMap<String, AtomicLong> categoryStats = new ConcurrentHashMap<>();
  //  private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

    @jakarta.annotation.PostConstruct
    public void initialize() {
        String[] categories = { "Electronics", "Clothing", "Books", "Home", "Sports" };
        for (String category : categories) {
            categoryStats.put(category, new AtomicLong(0));
        }
    }

    public String getAnalyticsSummary() {
        long stamp = stampedLock.tryOptimisticRead();
        String summary = generateSummary();

        if (!stampedLock.validate(stamp)) {
            stamp = stampedLock.readLock();
            try {
                summary = generateSummary();
            } finally {
                stampedLock.unlockRead(stamp);
            }
        }
        return summary;
    }

    private String generateSummary() {
        return String.format("Analytics Summary - Total Revenus: %d, Categories tracked : %d,Queue size : %d",
                totalRevenue.get(), categoryStats.size(), orderService.getQueueSize());
    }
    
    @Scheduled(fixedRate = 30000)
    public void updatedAnalytics() {
        long stamp = stampedLock.writeLock();

        try {
            totalRevenue.addAndGet(ThreadLocalRandom.current().nextLong(100, 1000)); // random
            /*In a multi-threaded environment, Random can cause contention.
            ThreadLocalRandom gives each thread its own instance, so it’s faster and safer in concurrent code. */

            String[] categories = { "Electronics\", \"Clothing\", \"Books\", \"Home" };
            for (String category : categories) {
                categoryStats.computeIfAbsent(category, k -> new AtomicLong(0))
                        .addAndGet(ThreadLocalRandom.current().nextLong(1, 10));
            }
        } finally {
            stampedLock.unlockWrite(stamp);
        }
    }
    
    public Map<String, Long> getCategoryStats() {
        return categoryStats.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().get()));
    }
    /*
     * We use AtomicLong instead of Long to ensure thread-safe, lock-free operations
     * (like increment, add, get) without data races in concurrent/multithreaded
     * environments.
     * 
     */
}
