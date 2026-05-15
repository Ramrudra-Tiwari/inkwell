# Post Service - Quick Reference Guide

## 🚀 Start the Service

```bash
cd D:\inkwell\post-service\post-service
./mvnw spring-boot:run
```

Service runs on: **http://localhost:8082**

---

## 📚 API Quick Reference

### Create a Post
```bash
curl -X POST http://localhost:8082/api/v1/posts \
  -H "Content-Type: application/json" \
  -d '{
    "authorId": 1,
    "title": "My First Blog Post",
    "content": "<p>This is the content with HTML tags...</p>",
    "excerpt": "A brief summary",
    "featuredImageUrl": "https://example.com/image.jpg"
  }'
```

### Get All Posts
```bash
curl http://localhost:8082/api/v1/posts
```

### Get Published Posts Only
```bash
curl http://localhost:8082/api/v1/posts/published
```

### Get Post by ID
```bash
curl http://localhost:8082/api/v1/posts/1
```

### Get Post by Slug
```bash
curl http://localhost:8082/api/v1/posts/slug/my-first-blog-post
```

### Get Posts by Author
```bash
curl http://localhost:8082/api/v1/posts/author/1
```

### Search Posts
```bash
curl "http://localhost:8082/api/v1/posts/search?keyword=spring"
```

### Update Post
```bash
curl -X PUT http://localhost:8082/api/v1/posts/1 \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Updated Title",
    "content": "<p>Updated content...</p>"
  }'
```

### Publish Post
```bash
curl -X POST http://localhost:8082/api/v1/posts/1/publish
```

### Like Post
```bash
curl -X POST http://localhost:8082/api/v1/posts/1/like
```

### Unlike Post
```bash
curl -X POST http://localhost:8082/api/v1/posts/1/unlike
```

### Delete Post
```bash
curl -X DELETE http://localhost:8082/api/v1/posts/1
```

---

## 🧪 Running Tests

### Run All Tests
```bash
./mvnw test
```

### Run Specific Test
```bash
./mvnw test -Dtest=PostServiceImplTest
```

### Expected Result
```
Tests run: 20, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

---

## 🔍 Key Code Locations

| Component | File | Key Method |
|-----------|------|-----------|
| Entity | `entity/Post.java` | - |
| Repository | `repository/PostRepository.java` | `incrementViewCount()` |
| Service | `service/impl/PostServiceImpl.java` | `calculateReadTime()` |
| Controller | `controller/PostResource.java` | All endpoints |
| Mapper | `mapper/PostMapper.java` | `toDTO()` |
| Slug Util | `util/SlugUtil.java` | `generateSlug()` |
| Exception Handler | `exception/GlobalExceptionHandler.java` | - |

---

## 📖 Important Concepts

### Slug Generation
```java
"My First Blog Post" → "my-first-blog-post"
```
- Automatic on post creation
- Unique constraint prevents duplicates
- Regenerated if title is updated

### Read Time Calculation
```
Words per minute: 200
Formula: max(1, words / 200)

Example:
- 100 words = 1 minute
- 200 words = 1 minute
- 400 words = 2 minutes
- 600 words = 3 minutes
```

### Atomic Increments
```java
// Safe for concurrent access - no race conditions
postRepository.incrementViewCount(postId);
postRepository.incrementLikesCount(postId);
```

### Post Status Lifecycle
```
DRAFT → PUBLISHED → UNPUBLISHED
  ↓
ARCHIVED
```

---

## 🛠️ Development Workflow

### 1. Add New Feature to Repository
```java
@Repository
public interface PostRepository extends JpaRepository<Post, Integer> {
    // Add new method here
    List<Post> findByCustomCondition(...);
}
```

### 2. Add Service Method
```java
@Service
public class PostServiceImpl implements PostService {
    @Override
    public List<PostDTO> getCustomPosts(...) {
        // Implementation here
        return postRepository.findByCustomCondition(...);
    }
}
```

### 3. Add Controller Endpoint
```java
@RestController
@RequestMapping("/api/v1/posts")
public class PostResource {
    @GetMapping("/custom")
    @Operation(summary = "Get custom posts")
    public ResponseEntity<List<PostDTO>> getCustomPosts(...) {
        return ResponseEntity.ok(postService.getCustomPosts(...));
    }
}
```

### 4. Add Unit Tests
```java
@Test
void testCustomFeature() {
    // Arrange
    when(...).thenReturn(...);
    
    // Act
    var result = postService.getCustomPosts(...);
    
    // Assert
    assertNotNull(result);
}
```

---

## 📊 Database Schema

### Posts Table
```sql
CREATE TABLE posts (
    post_id INT PRIMARY KEY AUTO_INCREMENT,
    author_id INT NOT NULL,
    title VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL UNIQUE,
    content LONGTEXT NOT NULL,
    excerpt VARCHAR(500),
    featured_image_url VARCHAR(500),
    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    read_time_min INT NOT NULL DEFAULT 1,
    view_count INT NOT NULL DEFAULT 0,
    likes_count INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    published_at DATETIME,
    
    INDEX idx_slug (slug),
    INDEX idx_author_id (author_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
);
```

---

## 🔐 Security

### Endpoint Permissions
- All endpoints are currently **PUBLIC** (for MVP)
- In production, add authorization:
  - `POST /api/v1/posts`: Authors only
  - `PUT /api/v1/posts/{id}`: Author of post only
  - `DELETE /api/v1/posts/{id}`: Author or Admin only
  - `GET /api/v1/posts/published`: Public
  - `GET /api/v1/posts/{id}`: Public

### Future Auth Integration
```java
// To be added in SecurityConfig
.requestMatchers("/api/v1/posts", "PUT", "DELETE").hasRole("AUTHOR")
.requestMatchers("/api/v1/posts/published", "GET").permitAll()
```

---

## 🐛 Troubleshooting

### Issue: Port 8082 already in use
```bash
# Change port in application.yml
server.port: 8083
```

### Issue: Database connection failed
```bash
# Check MySQL is running
# Verify credentials in application.yml
# Create database: CREATE DATABASE inkwell_post;
```

### Issue: Tests failing
```bash
# Run with verbose output
./mvnw test -e -X

# Clean and rebuild
./mvnw clean test
```

### Issue: Swagger not loading
```
# Ensure SecurityConfig permits /swagger-ui/**
# Check application.yml has springdoc configuration
# Access: http://localhost:8082/swagger-ui.html
```

---

## 📈 Performance Tips

### 1. Database Indexes
- ✅ Already configured on: slug, author_id, status, created_at

### 2. Caching (Future)
```java
@Cacheable("posts")
public PostDTO getPostBySlug(String slug) { ... }
```

### 3. Pagination (Future)
```java
List<PostDTO> getAllPosts(Pageable pageable);
// GET /api/v1/posts?page=0&size=20
```

### 4. Search Optimization
- Consider Elasticsearch for full-text search at scale

---

## 📝 Logging

### View Logs
```bash
# In console during development
# Set level in application.yml
logging.level.com.inkwell.post: DEBUG
```

### Example Log Statements
```
[INFO] Creating new post with title: My First Post
[DEBUG] Generated slug 'my-first-post' from title 'My First Post'
[DEBUG] Calculated read time: 5 minutes for 1000 words
[INFO] Post created successfully with ID: 1 and slug: my-first-post
[INFO] Publishing post with ID: 1
```

---

## 🎓 Learning Path

1. **Basics**: Read `README.md` for overview
2. **Entity**: Study `Post.java` for data model
3. **Repository**: Examine `PostRepository.java` for DB queries
4. **Service**: Learn business logic in `PostServiceImpl.java`
5. **Controller**: Review endpoints in `PostResource.java`
6. **Testing**: Study unit tests in `PostServiceImplTest.java`
7. **Integration**: Check API Gateway configuration

---

## 🔗 Related Services

- **Auth Service**: Port 8081 - User authentication
- **API Gateway**: Port 8080 - Request routing
- **Discovery Server**: Port 8761 - Service discovery
- **Post Service**: Port 8082 - Blog posts (this service)

---

## 📞 Support

For issues or questions:
1. Check logs: `./mvnw spring-boot:run`
2. Review test cases: `src/test/java/com/inkwell/post/`
3. Read Javadoc comments in source files
4. Check `README.md` and `IMPLEMENTATION_SUMMARY.md`

---

## ✅ Checklist for New Developer

- [ ] Clone repository
- [ ] Install Java 17+
- [ ] Install MySQL and create database
- [ ] Run `./mvnw clean install`
- [ ] Run `./mvnw test` (verify 20/20 pass)
- [ ] Run `./mvnw spring-boot:run`
- [ ] Access Swagger: http://localhost:8082/swagger-ui.html
- [ ] Try sample API calls
- [ ] Review code structure
- [ ] Read Javadoc comments
- [ ] Ready to contribute!

---

**Last Updated**: April 23, 2026
**Version**: 1.0.0
**Status**: ✅ Production Ready

