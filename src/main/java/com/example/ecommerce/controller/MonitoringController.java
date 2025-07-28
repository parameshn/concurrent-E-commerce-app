package com.example.ecommerce.controller;

import com.example.ecommerce.service.AnalyticsService;
import com.example.ecommerce.service.CacheService;
import com.example.ecommerce.service.BatchProcessingService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/monitoring")
public class MonitoringController {

    @Autowired
    private AnalyticsService analyticsService;

    @Autowired
    private CacheService cacheService;

    @Autowired
    private BatchProcessingService batchProcessingService;

    @GetMapping("/analytics")
    public ResponseEntity<String> getAnalytics() {
        return ResponseEntity.ok(analyticsService.getAnalyticsSummary());
    }

    @GetMapping("/cache/size")
    public ResponseEntity<Integer> getCacheSize() {
        return ResponseEntity.ok(cacheService.getCacheSize());
    }

    @GetMapping("/analytics/categories")
    public ResponseEntity<Map<String, Long>> getCategoryStats() {
        return ResponseEntity.ok(analyticsService.getCategoryStats());
    }

    @PostMapping("/cache/{key}")
    public ResponseEntity<String> putCache(@PathVariable String key, @RequestBody String value) {
        cacheService.put(key, value);
        return ResponseEntity.ok("Cached:" + key);
    }

    @GetMapping("/cache/{key}")
    public ResponseEntity<Optional<String>> getCacheEntry(@PathVariable String key) {
        Optional<String> value = cacheService.get(key);
        if (value != null) {
            return ResponseEntity.ok(value);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/thread-info")
    public ResponseEntity<String> getThreadInfo() {
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        return ResponseEntity.ok(String.format("Active threads: %d, Peak Threads: %d,Total started: %d",
                threadBean.getCurrentThreadCpuTime(),
                threadBean.getPeakThreadCount(),
                threadBean.getTotalStartedThreadCount()));
    }

    @GetMapping("/memory-info")
    public ResponseEntity<String> getMemoryInfo() {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;

        return ResponseEntity.ok(String.format("Total: %d MB, Used: %d MB, Free: %d MB, Max: %d MB",
                totalMemory / (1024 * 1024),
                usedMemory / (1024 * 1024),
                freeMemory / (1024 * 1024),
                runtime.maxMemory() / (1024 * 1024)));
    }

}
