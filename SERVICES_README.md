# Payment Service & Notification Service - Complete Implementation

This project contains two fully-featured microservices built with Spring Boot 3.3.5, demonstrating advanced Java concepts and modern cloud-native patterns.

## Overview

### Payment Service
A complete payment processing system with advanced features:
- **Entities**: Customer, Payment, Invoice with one-to-many and many-to-one relationships
- **Database**: H2 (configurable for PostgreSQL/MySQL)
- **Port**: 8082
- **Features**:
  - CompletableFuture for async operations
  - Virtual threads for better concurrency (Java 17+)
  - Advanced JPA queries with @Query annotations
  - Pagination, sorting, and filtering
  - Exception handling with sealed interfaces
  - Date-time functionalities with LocalDateTime
  - Java Collections (ArrayList, HashMap, etc.)
  - Thread-safe operations with Version control (optimistic locking)
  - JOIN operations in queries

### Notification Service  
An event-driven notification system with Kafka integration:
- **Entities**: Recipient, Notification, NotificationTemplate with relationships
- **Database**: H2 (configurable)
- **Port**: 8083
- **Features**:
  - Async Kafka producer for event publishing
  - Async Kafka consumer for event consumption
  - Virtual thread-based ExecutorService
  - Retry logic with error handling
  - CompletableFuture for async flows
  - Sealed interfaces for type-safe exception handling
  - Pagination and filtering
  - Manual offset management in Kafka
  - Concurrency control with Version fields

## Architecture

### Payment Service Architecture

```
PaymentServiceApplication
├── Entity Layer
│   ├── Customer (One-to-Many: Payments)
│   ├── Payment (Many-to-One: Customer, Invoice)
│   └── Invoice (One-to-Many: Payments)
├── Repository Layer (with @Query)
│   ├── PaymentRepository
│   ├── CustomerRepository
│   └── InvoiceRepository
├── Service Layer (CompletableFuture)
│   ├── PaymentService/PaymentServiceImpl
│   ├── CustomerService/CustomerServiceImpl
│   └── InvoiceService/InvoiceServiceImpl
├── Controller Layer (Async REST)
│   ├── PaymentController
│   ├── CustomerController
│   └── InvoiceController
├── Exception Layer (Sealed Interface)
│   ├── AppException (sealed)
│   ├── PaymentException
│   ├── ValidationException
│   ├── ResourceNotFoundException
│   └── ConcurrencyException
└── Tests
    ├── PaymentServiceImplTest
    ├── CustomerServiceImplTest
    └── PaymentControllerTest
```

### Notification Service Architecture

```
NotificationServiceApplication
├── Entity Layer
│   ├── Recipient (One-to-Many: Notifications)
│   ├── Notification (Many-to-One: Recipient, Template)
│   └── NotificationTemplate (One-to-Many: Notifications)
├── Repository Layer (with @Query)
│   ├── NotificationRepository
│   ├── RecipientRepository
│   └── NotificationTemplateRepository
├── Kafka Layer
│   ├── KafkaProducerConfig
│   ├── KafkaConsumerConfig
│   ├── NotificationProducer (Async)
│   └── NotificationConsumer (Async)
├── Service Layer (CompletableFuture)
│   ├── NotificationService/NotificationServiceImpl
├── Controller Layer (Async REST)
│   ├── NotificationController
├── Exception Layer (Sealed Interface)
│   ├── NotificationException (sealed)
│   ├── NotificationSendException
│   ├── InvalidNotificationException
│   └── NotificationProcessingException
└── Tests
    ├── NotificationServiceImplTest
    └── NotificationProducerTest
```

## Key Features Implemented

### 1. **Sealed Interfaces for Exception Handling**
```java
// AppException (sealed)
public sealed interface AppException permits PaymentException, ValidationException, 
    ResourceNotFoundException, ConcurrencyException

// Type-safe exception handling with specific implementations
```

### 2. **CompletableFuture & Async Operations**
```java
// All service methods return CompletableFuture
public CompletableFuture<PaymentDTO> createPayment(PaymentDTO paymentDTO)
public CompletableFuture<PageResponse<PaymentDTO>> getAllPayments(Pageable pageable)
```

### 3. **Java Collections & Thread Concurrency**
```java
// Virtual threads via ExecutorService
ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();

// Collections usage
List<Payment> payments = new ArrayList<>();
Map<String, Object> configProps = new HashMap<>();
Set<Long> ids = new HashSet<>();
```

### 4. **Advanced JPA Queries with @Query**

**Payment Service Queries:**
- Find by customer with custom ordering
- Date range filtering with JOINs
- Amount range queries
- Custom aggregations (SUM, COUNT)
- FETCH JOIN for eager loading
- Complex WHERE conditions

**Notification Service Queries:**
- Find pending retries with retry count limit
- Status-based queries with ordering
- Recipient-based filtering with joins
- Date range queries with full entity fetch
- Error tracking and analysis

### 5. **Pagination, Sorting, and Filtering**
```java
// All endpoints support pagination
Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

// Examples:
// GET /api/payments?page=0&size=10&sortBy=paymentDate&direction=DESC
// GET /api/customers/search?name=John&page=0&size=5&sortBy=name&direction=ASC
// GET /api/notifications/date-range?startDate=...&endDate=...&page=0
```

### 6. **Date-Time Functionalities**
```java
// LocalDateTime for creation and updates
private LocalDateTime createdAt;
private LocalDateTime updatedAt;
private LocalDateTime paymentDate;
private LocalDate invoiceDate;
private LocalDate dueDate;

// Automatic timestamp management
@PrePersist
protected void onCreate() {
    createdAt = LocalDateTime.now();
    updatedAt = LocalDateTime.now();
}

@PreUpdate
protected void onUpdate() {
    updatedAt = LocalDateTime.now();
}
```

### 7. **One-to-Many & Many-to-One Mappings**

**Payment Service:**
```java
// Customer (One) -> Payments (Many)
@OneToMany(mappedBy = "customer", cascade = CascadeType.ALL)
private List<Payment> payments;

// Payment (Many) -> Customer (One)
@ManyToOne(fetch = FetchType.LAZY)
private Customer customer;

// Payment (Many) -> Invoice (One)
@ManyToOne(fetch = FetchType.LAZY)
private Invoice invoice;
```

**Notification Service:**
```java
// Recipient (One) -> Notifications (Many)
@OneToMany(mappedBy = "recipient", cascade = CascadeType.ALL)
private List<Notification> notifications;

// Notification (Many) -> Recipient (One)
@ManyToOne(fetch = FetchType.LAZY)
private Recipient recipient;

// Notification (Many) -> Template (One)
@ManyToOne(fetch = FetchType.LAZY)
private NotificationTemplate template;
```

### 8. **Async Kafka Integration (Notification Service)**

**Kafka Topics:**
- `payment-notifications` - For payment-related events
- `system-notifications` - For system events

**Async Producer:**
```java
public CompletableFuture<Void> sendPaymentNotificationAsync(KafkaNotificationEvent event)
public CompletableFuture<Void> sendSystemNotificationAsync(KafkaNotificationEvent event)
public CompletableFuture<Void> batchSendNotificationsAsync(List<KafkaNotificationEvent> events)
```

**Async Consumer:**
```java
@KafkaListener(topics = "payment-notifications", groupId = "notification-service-group")
public void consumePaymentNotification(KafkaNotificationEvent event, Acknowledgment acknowledgment)

@KafkaListener(topics = "system-notifications", groupId = "notification-service-group")
public void consumeSystemNotification(KafkaNotificationEvent event, Acknowledgment acknowledgment)
```

### 9. **Version Control (Optimistic Locking)**
```java
@Version
private Long version;

// Prevents concurrent modification conflicts
// Database handles automatic version incrementation
```

### 10. **Comprehensive Unit Tests**

**Test Coverage:**
- Service layer tests with Mockito
- Controller endpoint tests
- Exception handling tests
- Async operation tests with ExecutionException handling
- Kafka producer tests
- Pagination and sorting tests
- Repository query tests

## Building and Running

### Prerequisites
- Java 17+
- Maven 3.8+
- Kafka 3.x (for notification-service)
- Docker (optional)

### Payment Service
```bash
cd payment-service
mvn clean package
mvn spring-boot:run

# Or with Java
java -jar target/payment-service-0.0.1-SNAPSHOT.jar
```

### Notification Service (with Kafka)
```bash
cd notification-service

# Start Kafka first
docker-compose up -d

mvn clean package
mvn spring-boot:run

# Or with Java
java -jar target/notification-service-0.0.1-SNAPSHOT.jar
```

## API Endpoints

### Payment Service

#### Customer Endpoints
```
POST   /api/customers                    - Create customer
GET    /api/customers/{id}               - Get customer
GET    /api/customers?page=0&size=10     - Get all customers (paginated)
GET    /api/customers/search?name=John   - Search by name
PUT    /api/customers/{id}               - Update customer
DELETE /api/customers/{id}               - Delete customer
```

#### Payment Endpoints
```
POST   /api/payments                     - Create payment
GET    /api/payments/{id}                - Get payment
GET    /api/payments?page=0&size=10      - Get all payments (paginated, sortable)
GET    /api/payments/customer/{id}       - Get customer payments
GET    /api/payments/status/{status}     - Get payments by status
GET    /api/payments/date-range?startDate=...&endDate=... - Date range query
GET    /api/payments/amount-range?minAmount=...&maxAmount=... - Amount range query
PUT    /api/payments/{id}                - Update payment
DELETE /api/payments/{id}                - Delete payment
GET    /api/payments/customer/{id}/total-paid - Get total paid
```

#### Invoice Endpoints
```
POST   /api/invoices                     - Create invoice
GET    /api/invoices/{id}                - Get invoice
GET    /api/invoices?page=0&size=10      - Get all invoices
GET    /api/invoices/status/{status}     - Get by status
GET    /api/invoices/date-range?...      - Get by date range
GET    /api/invoices/overdue             - Get overdue invoices
PUT    /api/invoices/{id}                - Update invoice
DELETE /api/invoices/{id}                - Delete invoice
```

### Notification Service

#### Notification Endpoints
```
POST   /api/notifications                - Send notification
POST   /api/notifications/async/kafka    - Send via Kafka (async)
GET    /api/notifications/{id}           - Get notification
GET    /api/notifications?page=0&size=10 - Get all notifications
GET    /api/notifications/recipient/{id} - Get recipient notifications
GET    /api/notifications/status/{status} - Get by status
GET    /api/notifications/date-range?... - Get by date range
POST   /api/notifications/retry-failed   - Retry failed notifications
GET    /api/notifications/failed/count   - Get failed count
```

## Database Schema

### Payment Service Tables
- `customers` - Customer information
- `invoices` - Invoice records
- `payments` - Payment transactions

### Notification Service Tables
- `recipients` - Notification recipients
- `notification_templates` - Template definitions
- `notifications` - Notification records

## Running Tests

```bash
# Payment Service
cd payment-service
mvn test

# Notification Service
cd notification-service
mvn test

# With coverage
mvn test jacoco:report
```

## Configuration

### Payment Service (application.yml)
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:paymentdb
  jpa:
    hibernate:
      ddl-auto: update
server:
  port: 8082
```

### Notification Service (application.yml)
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:notificationdb
  kafka:
    bootstrap-servers: localhost:9092
    producer:
      acks: all
    consumer:
      group-id: notification-service-group
server:
  port: 8083
```

## Error Handling

All services implement global exception handlers with consistent error responses:

```json
{
  "errorCode": "INVALID_AMOUNT",
  "message": "Payment amount must be greater than zero",
  "timestamp": "2024-05-20T10:30:00",
  "path": "/api/payments"
}
```

## Performance Features

1. **Virtual Threads**: Efficient concurrent execution
2. **Lazy Loading**: Reduced database queries with FetchType.LAZY
3. **Connection Pooling**: Managed by Spring Data JPA
4. **Batch Processing**: Batch Kafka message publishing
5. **Caching**: Query optimization through proper indexing
6. **Optimistic Locking**: Version-based concurrency control

## Security Considerations

- Input validation on all endpoints
- Proper exception handling to avoid information leakage
- H2 console disabled in production
- Eureka integration for service discovery
- Transaction management for data consistency

## Future Enhancements

- [ ] Add Spring Security with JWT authentication
- [ ] Implement caching with Redis
- [ ] Add OpenAPI/Swagger documentation
- [ ] Integrate with payment gateways (Stripe, PayPal)
- [ ] Add metrics with Micrometer
- [ ] Circuit breaker pattern with Resilience4j
- [ ] Database migration with Liquibase/Flyway
- [ ] Event sourcing for audit trail

## Notes

- Both services use H2 in-memory database for testing
- Virtual threads require Java 19+ for production use
- Kafka requires external infrastructure (local or Docker)
- Eureka discovery server for microservices communication
- Proper logging at INFO and DEBUG levels

## Support

For issues or improvements, refer to the test files for usage examples and best practices.
