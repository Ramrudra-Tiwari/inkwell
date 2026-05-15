# Comment Service - Quick Reference

## 🚀 Quick Start Commands

```bash
# Run the service
./mvnw spring-boot:run

# Run tests
./mvnw test

# Build JAR
./mvnw clean package

# Compile only
./mvnw compile

# View dependencies
./mvnw dependency:tree
```

## 📡 Service Information

- **Port**: 8083
- **Service Name**: comment-service
- **Database**: inkwell_comment
- **Swagger UI**: http://localhost:8083/swagger-ui.html
- **Health Check**: http://localhost:8083/actuator/health

## 🔗 API Endpoints Reference

### Create Operations

#### Create Top-Level Comment
```bash
POST /api/v1/comments
Content-Type: application/json

{
  "postId": 1,
  "authorId": 123,
  "content": "This is a great article!"
}
```

**Response**: 201 Created
```json
{
  "commentId": 1,
  "postId": 1,
  "authorId": 123,
  "content": "This is a great article!",
  "likesCount": 0,
  "status": "APPROVED",
  "createdAt": "2026-04-23T16:51:32",
  "updatedAt": "2026-04-23T16:51:32"
}
```

#### Create Reply (2nd Level Only)
```bash
POST /api/v1/comments
Content-Type: application/json

{
  "postId": 1,
  "authorId": 456,
  "parentCommentId": 1,
  "content": "I completely agree!"
}
```

### Read Operations

#### Get Comment by ID
```bash
GET /api/v1/comments/1
```

#### Get All Comments for Post
```bash
GET /api/v1/comments/post/1
```

#### Get Top-Level Comments Only
```bash
GET /api/v1/comments/post/1/top-level
```

#### Get Replies for Comment
```bash
GET /api/v1/comments/1/replies
```

### Update Operations

#### Update Comment Content
```bash
PUT /api/v1/comments/1
Content-Type: application/json

{
  "postId": 1,
  "authorId": 123,
  "content": "Updated comment content"
}
```

### Moderation Operations

#### Approve Comment
```bash
POST /api/v1/comments/1/approve
```

#### Reject Comment
```bash
POST /api/v1/comments/1/reject
```

### Engagement Operations

#### Like Comment
```bash
POST /api/v1/comments/1/like
```

#### Unlike Comment
```bash
POST /api/v1/comments/1/unlike
```

### Delete Operations

#### Soft Delete Comment
```bash
DELETE /api/v1/comments/1
```

## 📊 Response Examples

### Successful Comment Creation
```json
{
  "commentId": 1,
  "postId": 1,
  "authorId": 123,
  "parentCommentId": null,
  "content": "Great article!",
  "likesCount": 0,
  "status": "APPROVED",
  "createdAt": "2026-04-23T16:51:32.123",
  "updatedAt": "2026-04-23T16:51:32.123"
}
```

### Comment List Response
```json
[
  {
    "commentId": 1,
    "postId": 1,
    "authorId": 123,
    "content": "Great article!",
    "likesCount": 5,
    "status": "APPROVED",
    "createdAt": "2026-04-23T16:51:32.123",
    "updatedAt": "2026-04-23T16:51:32.123"
  },
  {
    "commentId": 2,
    "postId": 1,
    "authorId": 456,
    "parentCommentId": 1,
    "content": "I agree!",
    "likesCount": 2,
    "status": "APPROVED",
    "createdAt": "2026-04-23T16:51:33.456",
    "updatedAt": "2026-04-23T16:51:33.456"
  }
]
```

## ❌ Error Responses

### Comment Not Found
```json
{
  "timestamp": "2026-04-23T16:51:32",
  "status": 404,
  "error": "Comment Not Found",
  "message": "Comment not found with id: 999",
  "path": "/api/v1/comments/999"
}
```

### Validation Error
```json
{
  "timestamp": "2026-04-23T16:51:32",
  "status": 400,
  "error": "Validation Failed",
  "message": "Input validation failed",
  "path": "/api/v1/comments",
  "validationErrors": {
    "content": "Content cannot be blank",
    "postId": "Post ID is required"
  }
}
```

### Threading Violation
```json
{
  "timestamp": "2026-04-23T16:51:32",
  "status": 400,
  "error": "Bad Request",
  "message": "Cannot reply to a reply. Threading is limited to 2 levels.",
  "path": "/api/v1/comments"
}
```

## 🗄️ Database Schema

```sql
CREATE TABLE comments (
    comment_id INT AUTO_INCREMENT PRIMARY KEY,
    post_id INT NOT NULL,
    author_id INT NOT NULL,
    parent_comment_id INT,
    content LONGTEXT NOT NULL,
    likes_count INT NOT NULL DEFAULT 0,
    status ENUM('APPROVED', 'PENDING', 'REJECTED', 'DELETED') NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    INDEX idx_post_id (post_id),
    INDEX idx_author_id (author_id),
    INDEX idx_parent_comment_id (parent_comment_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
);
```

## 🧪 Testing Commands

### Run All Tests
```bash
./mvnw test
```

### Run Specific Test Class
```bash
./mvnw test -Dtest=CommentServiceImplTest
```

### Run with Coverage
```bash
./mvnw test jacoco:report
```

### Debug Tests
```bash
./mvnw test -Dmaven.surefire.debug=true
```

## 🔧 Configuration Reference

### application.yml
```yaml
server:
  port: 8083

spring:
  application:
    name: comment-service
  datasource:
    url: jdbc:mysql://localhost:3306/inkwell_comment
    username: root
    password: rootroot
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/

logging:
  level:
    com.inkwell.comment: INFO
```

## 📈 Monitoring Endpoints

- **Health**: `GET /actuator/health`
- **Info**: `GET /actuator/info`
- **Metrics**: `GET /actuator/metrics`
- **Swagger**: `GET /swagger-ui.html`

## 🐳 Docker Commands

### Build Image
```bash
docker build -t inkwell/comment-service .
```

### Run Container
```bash
docker run -p 8083:8083 inkwell/comment-service
```

### Docker Compose
```yaml
version: '3.8'
services:
  comment-service:
    image: inkwell/comment-service
    ports:
      - "8083:8083"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
    depends_on:
      - mysql
      - eureka
```

## 🔍 Common Issues & Solutions

### Database Connection Failed
```
Caused by: java.net.ConnectException: Connection refused
```
**Solution**: Ensure MySQL is running on port 3306

### Port Already in Use
```
Web server failed to start. Port 8083 was already in use.
```
**Solution**: Change port in application.yml or kill process using the port

### Eureka Connection Failed
```
DiscoveryClient: comment-service - registration failed
```
**Solution**: Ensure Eureka server is running on port 8761

### Compilation Errors
```
[ERROR] Failed to execute goal org.apache.maven.plugins:maven-compiler-plugin
```
**Solution**: Check Java version (requires Java 17+)

## 📝 Development Notes

### Threading Rules
- Comments can be top-level (parentCommentId = null)
- Replies can only be to top-level comments
- No nested replies beyond 2 levels

### Status Values
- **APPROVED**: Visible, default for new comments
- **PENDING**: Awaiting moderation
- **REJECTED**: Hidden due to policy violation
- **DELETED**: Soft-deleted, content cleared

### Atomic Operations
- Like/unlike operations are atomic to prevent race conditions
- Uses database-level increment/decrement queries

### Validation Rules
- Content cannot be blank
- PostId and AuthorId are required
- Parent comment must exist and be approved
- Cannot reply to replies

## 🎯 Performance Tips

### Database Indexes
- Composite indexes on frequently queried columns
- Foreign key indexes for joins
- Status and date indexes for filtering

### Query Optimization
- Use pagination for large comment lists
- Eager loading for related entities when needed
- Database-level constraints for data integrity

### Caching Strategy
- Consider Redis caching for popular posts' comments
- Cache comment counts and metadata
- Invalidate cache on comment operations
