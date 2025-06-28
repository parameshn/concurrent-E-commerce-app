package com.example.ecommerce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EnableAsync
@EnableScheduling
public class ConcurrentECommerceApplication {

    public static  void main(String[] args){
        SpringApplication.run(ConcurrentECommerceApplication.class,args);
    }
}
/*
 * Why @EnableAsync is needed
 * Without @EnableAsync, any @Async methods will not actually run
 * asynchronously—Spring will simply ignore the annotation.
 * 
 * By placing @EnableAsync on your @SpringBootApplication class, you're telling
 * Spring to:
 * 
 * Scan for methods annotated with @Async
 * 
 * Wrap them in proxies that delegate to a thread pool (like the one defined in
 * your ConcurrencyConfig.java)
 */