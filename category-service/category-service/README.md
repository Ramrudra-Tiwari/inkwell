# Category & Tag Microservice - InkWell Blogging Platform

## Overview

The **Category & Tag Microservice** provides hierarchical category management and flat tag organization for the InkWell Blogging Platform. This service handles taxonomy operations, post associations, and maintains post counts for trending analysis.

## Features

### 🏗️ **Hierarchical Categories**
- Parent-child category relationships (e.g., "Programming" → "Java")
- Circular reference prevention
- Unlimited depth with validation

### 🏷️ **Flat Tag System**
- Simple tag creation and management
- Post tagging with atomic counters
- Trending tags by post count

### 📊 **Post Association**
- Link/unlink posts to categories and tags
- Atomic increment/decrement of post counts
- Thread-safe operations

### 🔍 **Search & Discovery**
- Search categories and tags by name
- Get trending tags
- Hierarchical category browsing

## Architecture

### Tech Stack
- **Spring Boot 3.5.13** - Framework
- **Spring Data JPA** - Data persistence
- **MySQL** - Database
- **Lombok** - Boilerplate reduction
- **MapStruct** - DTO mapping
- **Swagger/OpenAPI** - API documentation

### Database Schema

#### Categories Table
```sql
CREATE TABLE categories (
    category_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(255) UNIQUE NOT NULL,
    description TEXT,
    parent_category_id INT,
    post_count INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_slug (slug),
    INDEX idx_parent_id (parent_category_id),
    INDEX idx_created_at (created_at),
    FOREIGN KEY (parent_category_id) REFERENCES categories(category_id)
);
```

#### Tags Table
```sql
CREATE TABLE tags (
    tag_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(255) UNIQUE NOT NULL,
    post_count INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_slug (slug),
    INDEX idx_tag_created_at (created_at)
);
```

## API Endpoints

### Categories

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/categories` | Create category |
| GET | `/api/v1/categories/{id}` | Get category by ID |
| GET | `/api/v1/categories/slug/{slug}` | Get category by slug |
| GET | `/api/v1/categories` | Get all categories |
| GET | `/api/v1/categories/top-level` | Get root categories |
| GET | `/api/v1/categories/{id}/children` | Get child categories |
| PUT | `/api/v1/categories/{id}` | Update category |
| DELETE | `/api/v1/categories/{id}` | Delete category |
| GET | `/api/v1/categories/search?name=...` | Search categories |
| POST | `/api/v1/categories/{id}/posts` | Assign post to category |
| DELETE | `/api/v1/categories/{id}/posts/{postId}` | Remove post from category |

### Tags

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/tags` | Create tag |
| GET | `/api/v1/tags/{id}` | Get tag by ID |
| GET | `/api/v1/tags/slug/{slug}` | Get tag by slug |
| GET | `/api/v1/tags` | Get all tags |
| GET | `/api/v1/tags/trending?limit=10` | Get trending tags |
| PUT | `/api/v1/tags/{id}` | Update tag |
| DELETE | `/api/v1/tags/{id}` | Delete tag |
| GET | `/api/v1/tags/search?name=...` | Search tags |
| POST | `/api/v1/tags/{id}/posts` | Assign post to tag |
| DELETE | `/api/v1/tags/{id}/posts/{postId}` | Remove post from tag |

## Configuration

### Application Properties
```yaml
server:
  port: 8084

spring:
  application:
    name: category-service
  datasource:
    url: jdbc:mysql://localhost:3306/inkwell_category
    username: root
    password: rootroot
  jpa:
    hibernate:
      ddl-auto: update

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka
```

## Getting Started

### Prerequisites
- Java 17+
- MySQL 8.0+
- Maven 3.6+

### Installation

1. **Clone and navigate:**
   ```bash
   cd inkwell/category-service
   ```

2. **Compile:**
   ```bash
   ./mvnw clean compile
   ```

3. **Run tests:**
   ```bash
   ./mvnw test
   ```

4. **Start service:**
   ```bash
   ./mvnw spring-boot:run
   ```

### Database Setup
```sql
CREATE DATABASE inkwell_category;
```

## API Documentation

Access Swagger UI at: `http://localhost:8084/swagger-ui.html`

## Testing

### Unit Tests
- **14 comprehensive tests** covering:
  - Hierarchy validation (circular reference prevention)
  - Post count increment/decrement operations
  - CRUD operations with validation
  - Parent-child relationship management

### Test Coverage
- ✅ Category hierarchy validation
- ✅ Post association operations
- ✅ CRUD operations
- ✅ Search functionality
- ✅ Error handling

## Error Handling

### Standard Error Response
```json
{
  "timestamp": "2026-04-23T20:31:40.389+05:30",
  "status": 404,
  "error": "Taxonomy Not Found",
  "message": "Category not found with ID: 123",
  "path": "/api/v1/categories/123"
}
```

### Common HTTP Status Codes
- `200` - Success
- `201` - Created
- `204` - No Content
- `400` - Bad Request
- `404` - Not Found
- `409` - Conflict (duplicate slug)
- `500` - Internal Server Error

## Service Isolation

This microservice maintains **loose coupling** with other services:
- Uses `postId` integers for post associations
- No direct dependencies on Post or User entities
- Communicates via REST APIs through API Gateway

## Monitoring

### Health Check
```
GET /actuator/health
```

### Service Discovery
- **Eureka Server**: `http://localhost:8761`
- **Service Name**: `category-service`
- **Port**: `8084`

## Contributing

1. Follow existing code patterns
2. Add comprehensive unit tests
3. Update API documentation
4. Ensure service isolation
5. Test with other microservices

## License

This project is part of the InkWell Blogging Platform.
