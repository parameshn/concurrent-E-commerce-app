package com.example.ecommerce.service;

import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.StampedLock;
import java.util.stream.Collectors;

@Service
public class CacheService {

    private final ConcurrentHashMap<String, String> cache = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleanupExecutor = Executors.newSingleThreadScheduledExecutor();
    private final ConcurrentHashMap<String, Long> accessTimes = new ConcurrentHashMap<>();

    @PostConstruct
    public void startCleanupTask() {
        cleanupExecutor.scheduleAtFixedRate(this::cleanupExpiredEntries, 5, 5, TimeUnit.MINUTES);
    }

    public void put(String key, String value) {
        cache.put(key, value);
        accessTimes.put(key, System.currentTimeMillis());
    }
    

    
     public Optional<String> get(String key) {
        String value = cache.get(key);
        if (value != null) {
            accessTimes.put(key, System.currentTimeMillis());
            return Optional.of(value);
        }
        return Optional.empty();
    }


    private void cleanupExpiredEntries() {
        long currentTime = System.currentTimeMillis();
        long expirationTime = 10 * 60 * 1000;

        accessTimes.entrySet().removeIf(entry -> {
            if (currentTime - entry.getValue() > expirationTime) {
                cache.remove(entry.getKey());
                return true;
            }
            return false;
        });
    }

    public void remove(String key) {
        cache.remove(key);
        accessTimes.remove(key);
    }

    @PreDestroy
    public void cleanup() {
        cleanupExecutor.shutdown();
    }

    public int getCacheSize() {
        return cache.size();
    }
   
}
