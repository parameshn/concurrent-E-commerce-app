# Concurrent E-Commerce Spring Boot Application

**API Documentation:**  
**[View Complete API Collection in Postman](https://kxld-4969301.postman.co/workspace/Paramesh-workspace~638a0202-4881-45c9-8a40-544f0617cade/collection/44593529-6c6c65dc-c08a-410d-a2d9-8b5205c0b768?action=share&creator=44593529)**

A production-ready Spring Boot application demonstrating advanced concurrency patterns and thread-safe operations in an e-commerce domain. This project implements enterprise-grade concurrent programming techniques including optimistic and pessimistic locking, asynchronous processing, producer-consumer patterns, and thread-safe CRUD operations.

## Features

- **Thread-Safe CRUD Operations** utilizing ReentrantReadWriteLocks for optimal performance
- **Database Locking Strategies** with both optimistic and pessimistic locking mechanisms
- **Asynchronous Processing** leveraging CompletableFuture for non-blocking operations
- **Producer-Consumer Architecture** for scalable order processing workflows
- **Concurrent Caching Layer** implemented with ConcurrentHashMap
- **Batch Processing Framework** with configurable thread pool management
- **Real-time System Monitoring** and performance analytics
- **Integrated Load Testing** for performance validation

## Technology Stack

- **Java 17**
- **Spring Boot 3.2.0**
- **Spring Data JPA**
- **H2 Database** (In-memory)
- **Spring Boot Actuator**
- **Maven 3.6+**

## Prerequisites

- Java Development Kit 17 or higher
- Apache Maven 3.6 or higher
- Git version control system

## Installation and Setup

### Clone the Repository
```bash
git clone https://github.com/parameshn/concurrent-E-commerce-app.git
cd concurrent-E-commerce-app
```

### Build and Run the Application
```bash
mvn clean install
mvn spring-boot:run
```

The application will be accessible at `http://localhost:8080`

### Database Console Access
- **URL:** `http://localhost:8080/h2-console`
- **JDBC URL:** `jdbc:h2:mem:ecommerce`
- **Username:** `sa`
- **Password:** *(empty)*

## API Testing Examples

### Product Management
```bash
# Create a new product
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

### Concurrency Testing
```bash
# Execute concurrent read operations
curl -X POST http://localhost:8080/api/load-test/concurrent-reads/10/100

# Test concurrent update operations
curl -X POST http://localhost:8080/api/load-test/concurrent-updates/5/50
```

### System Monitoring
```bash
# Retrieve system analytics
curl http://localhost:8080/api/monitoring/analytics

# Monitor thread pool statistics
curl http://localhost:8080/api/monitoring/thread-info

# Check memory utilization
curl http://localhost:8080/api/monitoring/memory-info
```

## Architecture Overview

### Concurrency Patterns Implementation

1. **Read-Write Locking** - Optimized concurrent read access with exclusive write operations
2. **Optimistic Locking** - JPA-based version control with automatic retry mechanisms
3. **Pessimistic Locking** - Database-level locking for critical transaction handling
4. **StampedLock Implementation** - High-performance optimistic read operations for analytics
5. **Producer-Consumer Pattern** - Asynchronous order processing with blocking queue implementation
6. **Thread Pool Management** - Custom executor services for different operational contexts

### Core Service Components

- **ProductService** - Thread-safe product lifecycle management with integrated caching
- **OrderService** - Asynchronous order processing with queue-based workflow management
- **AnalyticsService** - Real-time performance metrics with StampedLock optimization
- **CacheService** - Concurrent caching implementation with automatic expiration
- **BatchProcessingService** - Parallel batch operation execution framework

## System Monitoring

| Endpoint | Description |
|----------|-------------|
| `/api/monitoring/analytics` | Comprehensive system analytics and metrics |
| `/api/monitoring/thread-info` | Thread pool utilization statistics |
| `/api/monitoring/memory-info` | JVM memory usage and allocation details |
| `/api/monitoring/cache/size` | Cache performance and utilization metrics |
| `/api/orders/queue/size` | Order processing queue status |

## Performance Testing

Built-in load testing endpoints for performance validation:

- **Concurrent Read Testing**: `/api/load-test/concurrent-reads/{threads}/{iterations}`
- **Concurrent Update Testing**: `/api/load-test/concurrent-updates/{threads}/{iterations}`
- **Batch Operation Testing**: `/api/load-test/batch-operations/{count}`

## Configuration

Application configuration parameters in `application.yml`:

```yaml
spring:
  task:
    execution:
      pool:
        core-size: 8
        max-size: 16
        queue-capacity: 500
    scheduling:
      pool:
        size: 4
```

## Troubleshooting

### Common Issues and Solutions

**Port Conflict (8080)**
```yaml
# Modify application.yml
server:
  port: 8081
```

**Memory Constraints During Load Testing**
```bash
export MAVEN_OPTS="-Xmx2g -XX:+UseG1GC"
mvn spring-boot:run
```

**Thread Pool Saturation**
- Monitor thread utilization via `/api/monitoring/thread-info`
- Adjust thread pool configuration based on system capacity

## Performance Metrics

The application provides comprehensive monitoring for:
- Thread pool utilization and performance
- Cache hit ratios and efficiency metrics
- Operation execution counters
- JVM memory usage patterns
- Queue processing statistics
- Response time analytics

## Contributing

We welcome contributions to enhance the project's functionality and performance.

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/enhancement-name`)
3. Implement changes with appropriate test coverage
4. Commit changes (`git commit -m 'Add feature: enhancement description'`)
5. Push to the branch (`git push origin feature/enhancement-name`)
6. Submit a Pull Request with detailed description

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for complete license terms.

---

**For comprehensive API documentation with request/response examples, please refer to the [Postman Collection](https://kxld-4969301.postman.co/workspace/Paramesh-workspace~638a0202-4881-45c9-8a40-544f0617cade/collection/44593529-6c6c65dc-c08a-410d-a2d9-8b5205c0b768?action=share&creator=44593529)**
