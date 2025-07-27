# Concurrent E-Commerce Spring Boot Application

**📚 Complete API Documentation with Examples:**  
**🔗 [View API Collection in Postman](https://kxld-4969301.postman.co/workspace/Paramesh-workspace~638a0202-4881-45c9-8a40-544f0617cade/collection/44593529-6c6c65dc-c08a-410d-a2d9-8b5205c0b768?action=share&creator=44593529)**

A comprehensive Spring Boot application demonstrating advanced concurrency patterns and thread-safe operations in an e-commerce context. This project showcases real-world concurrent programming techniques including optimistic/pessimistic locking, async processing, producer-consumer patterns, and thread-safe CRUD operations.

## 🚀 Features

- **Thread-Safe CRUD Operations** with ReentrantReadWriteLocks
- **Optimistic & Pessimistic Locking** for data consistency
- **Asynchronous Processing** with CompletableFuture
- **Producer-Consumer Pattern** for order processing
- **Concurrent Caching** with ConcurrentHashMap
- **Batch Processing** with thread pools
- **Real-time Monitoring** and analytics
- **Load Testing Endpoints** for performance evaluation

## 🛠 Tech Stack

- **Java 17**
- **Spring Boot 3.2.0**
- **Spring Data JPA**
- **H2 Database** (In-memory)
- **Spring Actuator** for monitoring
- **Maven** for dependency management

## 📋 Prerequisites

- Java 17 or higher
- Maven 3.6+
- Git

## 🏃‍♂️ Quick Start

### 1. Clone the Repository
```bash
git clone <repository-url>
cd concurrent-ecommerce
```

### 2. Run the Application
```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

### 3. Access H2 Console (Optional)
- URL: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:ecommerce`
- Username: `sa`
- Password: *(leave empty)*



## 🧪 Quick API Tests

### Create a Product
```bash
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Gaming Laptop",
    "description": "High-performance gaming laptop",
    "price": 1299.99,
    "stockQuantity": 25,
    "category": "Electronics"
  }'
```

### Test Concurrent Operations
```bash
# Test concurrent reads (10 threads, 100 operations each)
curl -X POST http://localhost:8080/api/load-test/concurrent-reads/10/100

# Test concurrent stock updates
curl -X POST http://localhost:8080/api/load-test/concurrent-updates/5/50
```

### Monitor System Performance
```bash
# Get analytics summary
curl http://localhost:8080/api/monitoring/analytics

# Check thread information
curl http://localhost:8080/api/monitoring/thread-info

# Monitor memory usage
curl http://localhost:8080/api/monitoring/memory-info
```

## 🏗 Core Architecture

### Concurrency Patterns Implemented

1. **Read-Write Locks** - Efficient concurrent reads with exclusive writes
2. **Optimistic Locking** - JPA version-based conflict resolution with retry
3. **Pessimistic Locking** - Database-level locking for critical operations
4. **StampedLock** - High-performance optimistic reads in analytics
5. **Producer-Consumer** - Async order processing with blocking queues
6. **Thread Pools** - Custom executors for different operation types

### Key Services

- **ProductService** - Thread-safe product management with caching
- **OrderService** - Async order processing with queue management  
- **AnalyticsService** - Real-time statistics with StampedLock
- **CacheService** - Concurrent caching with automatic cleanup
- **BatchProcessingService** - Parallel batch operations

## 📊 Monitoring Endpoints

| Endpoint | Description |
|----------|-------------|
| `/api/monitoring/analytics` | System analytics summary |
| `/api/monitoring/thread-info` | Thread pool statistics |
| `/api/monitoring/memory-info` | JVM memory usage |
| `/api/monitoring/cache/size` | Cache statistics |
| `/api/orders/queue/size` | Order processing queue size |

## 🧪 Load Testing

The application includes built-in load testing endpoints:

- **Concurrent Reads**: `/api/load-test/concurrent-reads/{threads}/{iterations}`
- **Concurrent Updates**: `/api/load-test/concurrent-updates/{threads}/{iterations}`
- **Batch Operations**: `/api/load-test/batch-operations/{count}`

## 🔧 Configuration

Key configuration options in `application.yml`:

```yaml
spring:
  task:
    execution:
      pool:
        core-size: 8      # Core thread pool size
        max-size: 16      # Maximum thread pool size
        queue-capacity: 500
    scheduling:
      pool:
        size: 4           # Scheduled task pool size
```

## 🐛 Troubleshooting

### Common Issues

1. **Port 8080 already in use**
   ```bash
   # Change port in application.yml
   server:
     port: 8081
   ```

2. **Memory issues during load testing**
   ```bash
   # Increase JVM heap size
   export MAVEN_OPTS="-Xmx2g"
   mvn spring-boot:run
   ```

3. **Thread pool exhaustion**
   - Monitor thread usage via `/api/monitoring/thread-info`
   - Adjust pool sizes in configuration

## 📈 Performance Metrics

The application tracks:
- Thread pool utilization
- Cache hit ratios
- Operation counters
- Memory usage
- Queue sizes
- Response times

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- Spring Boot team for the excellent framework
- Java concurrency utilities documentation
- Community examples and best practices

---

**📖 For detailed API documentation and examples, visit the [Postman Collection](https://kxld-4969301.postman.co/workspace/Paramesh-workspace~638a0202-4881-45c9-8a40-544f0617cade/collection/44593529-6c6c65dc-c08a-410d-a2d9-8b5205c0b768?action=share&creator=44593529)**
