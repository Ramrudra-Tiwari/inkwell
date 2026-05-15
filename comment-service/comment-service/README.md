# Comment Service - InkWell Blogging Platform

## Overview

The Comment Service is a microservice in the InkWell Blogging Platform that manages blog post comments and threaded discussions. It provides comprehensive comment functionality including nested replies, moderation, and engagement features.

## Features

### Core Functionality
- **Comment Creation**: Create top-level comments and replies (limited to 2 levels)
- **Threading Logic**: Hierarchical comment structure with parent-child relationships
- **Soft Delete**: Preserve thread structure while removing inappropriate content
- **Moderation**: Approve/reject comments for content control
- **Engagement**: Like/unlike comments with atomic counters

### Technical Features
- **RESTful API**: Complete REST endpoints with Swagger documentation
- **Database Integration**: MySQL with JPA/Hibernate
- **Service Discovery**: Eureka client registration
- **Error Handling**: Comprehensive exception handling with consistent error responses
- **Validation**: Input validation with detailed error messages
- **Logging**: Structured logging for monitoring and debugging

## Architecture

### Entity Model
```java
Comment {
    commentId: Integer (PK)
    postId: Integer (FK to Post service)
    authorId: Integer (FK to Auth service)
    parentCommentId: Integer (nullable, for replies)
    content: String (TEXT)
    likesCount: Integer (default: 0)
    status: Enum (APPROVED, PENDING, REJECTED, DELETED)
    createdAt: LocalDateTime
    updatedAt: LocalDateTime
}
```

### Threading Rules
- **Level 1**: Top-level comments (parentCommentId = null)
- **Level 2**: Replies to top-level comments (parentCommentId points to level 1)
- **Limitation**: No replies to replies (maintains 2-level hierarchy)

### Status Lifecycle
- **APPROVED**: Visible to all users (default for new comments)
- **PENDING**: Awaiting moderation
- **REJECTED**: Hidden due to content violation
- **DELETED**: Soft-deleted with content cleared

## API Endpoints

### Comment Management
- `POST /api/v1/comments` - Create new comment/reply
- `GET /api/v1/comments/{id}` - Get comment by ID
- `PUT /api/v1/comments/{id}` - Update comment content
- `DELETE /api/v1/comments/{id}` - Soft delete comment

### Comment Retrieval
- `GET /api/v1/comments/post/{postId}` - Get all comments for a post
- `GET /api/v1/comments/post/{postId}/top-level` - Get only top-level comments
- `GET /api/v1/comments/{parentId}/replies` - Get replies for a comment

### Moderation & Engagement
- `POST /api/v1/comments/{id}/approve` - Approve comment
- `POST /api/v1/comments/{id}/reject` - Reject comment
- `POST /api/v1/comments/{id}/like` - Like comment
- `POST /api/v1/comments/{id}/unlike` - Unlike comment

## Quick Start

### Prerequisites
- Java 17+
- MySQL 8.0+
- Maven 3.6+

### Configuration
```yaml
server:
  port: 8083

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/inkwell_comment
    username: root
    password: rootroot
  jpa:
    hibernate:
      ddl-auto: update

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
```

### Running the Service
```bash
# Compile and run
./mvnw spring-boot:run

# Or build and run JAR
./mvnw clean package
java -jar target/comment-service-0.0.1-SNAPSHOT.jar
```

### Database Setup
```sql
CREATE DATABASE inkwell_comment;
-- Tables will be auto-created via Hibernate
```

## API Usage Examples

### Create a Comment
```bash
POST /api/v1/comments
Content-Type: application/json

{
  "postId": 1,
  "authorId": 123,
  "content": "Great article! Thanks for sharing."
}
```

### Create a Reply
```bash
POST /api/v1/comments
Content-Type: application/json

{
  "postId": 1,
  "authorId": 456,
  "parentCommentId": 1,
  "content": "I agree, very insightful!"
}
```

### Get Comments for a Post
```bash
GET /api/v1/comments/post/1
```

### Like a Comment
```bash
POST /api/v1/comments/1/like
```

## Error Handling

The service provides consistent error responses:

```json
{
  "timestamp": "2026-04-23T16:51:32",
  "status": 404,
  "error": "Comment Not Found",
  "message": "Comment not found with id: 1",
  "path": "/api/v1/comments/1"
}
```

### Common Error Codes
- `400 Bad Request`: Validation errors or invalid threading
- `404 Not Found`: Comment or parent comment not found
- `500 Internal Server Error`: Unexpected server errors

## Testing

### Unit Tests
```bash
./mvnw test
```

### Test Coverage
- Comment creation and validation
- Reply threading logic (parent existence, 2-level limit)
- Soft delete functionality
- Like/unlike operations
- Error scenarios and exception handling

## Service Discovery

The service registers with Eureka at startup:
- **Service Name**: comment-service
- **Port**: 8083
- **Health Check**: `/actuator/health`

## Monitoring

### Health Endpoints
- `GET /actuator/health` - Service health status
- `GET /actuator/info` - Service information
- `GET /actuator/metrics` - Application metrics

### Swagger Documentation
- **URL**: `http://localhost:8083/swagger-ui.html`
- **API Docs**: `http://localhost:8083/v3/api-docs`

## Development

### Project Structure
```
src/
├── main/java/com/inkwell/comment/
│   ├── CommentServiceApplication.java
│   ├── controller/
│   │   ├── CommentResource.java
│   │   └── HomeController.java
│   ├── dto/
│   │   ├── CommentDTO.java
│   │   └── CommentRequest.java
│   ├── entity/
│   │   ├── Comment.java
│   │   └── CommentStatus.java
│   ├── exception/
│   │   ├── CommentNotFoundException.java
│   │   ├── ErrorResponse.java
│   │   └── GlobalExceptionHandler.java
│   ├── mapper/
│   │   └── CommentMapper.java
│   ├── repository/
│   │   └── CommentRepository.java
│   └── service/
│       ├── CommentService.java
│       └── impl/
│           └── CommentServiceImpl.java
└── test/java/com/inkwell/comment/
    └── service/impl/
        └── CommentServiceImplTest.java
```

### Key Classes
- **CommentResource**: REST controller with all endpoints
- **CommentServiceImpl**: Business logic implementation
- **CommentRepository**: Data access layer with custom queries
- **GlobalExceptionHandler**: Centralized error handling

## Deployment

### Docker
```dockerfile
FROM openjdk:17-jdk-slim
COPY target/comment-service-*.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java","-jar","/app.jar"]
```

### Kubernetes
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: comment-service
spec:
  replicas: 2
  selector:
    matchLabels:
      app: comment-service
  template:
    metadata:
      labels:
        app: comment-service
    spec:
      containers:
      - name: comment-service
        image: inkwell/comment-service:latest
        ports:
        - containerPort: 8083
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "prod"
```

## Contributing

1. Follow the existing code style and patterns
2. Add unit tests for new functionality
3. Update Swagger documentation for API changes
4. Ensure all tests pass before submitting PR

## License

This project is part of the InkWell Blogging Platform.
