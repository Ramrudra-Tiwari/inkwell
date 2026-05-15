# Post Service - InkWell Blogging Platform

## Overview
The Post Service is a microservice responsible for managing blog posts in the InkWell Blogging Platform. It handles creation, reading, updating, and deletion of posts with advanced features like automatic slug generation, read-time calculation, and atomic view/like counters.

## Features

### Core Functionality
- ✅ **Create Posts**: Posts are initialized in DRAFT status
- ✅ **Read Posts**: Retrieve posts by ID, slug, or author
- ✅ **Update Posts**: Update title, content, status, and metadata
- ✅ **Delete Posts**: Permanently remove posts
- ✅ **Publish/Unpublish**: Manage post publication status

### Advanced Features
- ✅ **Automatic Slug Generation**: Titles are converted to URL-safe slugs (e.g., "My First Post" → "my-first-post")
- ✅ **Read Time Calculation**: Automatically calculates reading time based on 200 Words Per Minute (WPM)
- ✅ **Atomic Increments**: View and like counts use atomic operations to prevent race conditions during concurrent hits
- ✅ **Search Functionality**: Search posts by keyword across titles and content
- ✅ **Role-Based Access**: Supports different user roles (implicit via author IDs)

## Tech Stack
- **Framework**: Spring Boot 3.5.13
- **Database**: MySQL 8.0
- **Caching**: Redis
- **ORM**: Spring Data JPA
- **Mapping**: MapStruct
- **Testing**: Mockito + JUnit 5
- **Documentation**: Swagger/OpenAPI
- **Service Discovery**: Eureka Client

## Project Structure
```
post-service/
├── src/
│   ├── main/
│   │   ├── java/com/inkwell/post/
│   │   │   ├── controller/        # REST endpoints
│   │   │   ├── service/           # Business logic interfaces
│   │   │   ├── service/impl/      # Service implementations
│   │   │   ├── entity/            # JPA entities
│   │   │   ├── dto/               # Data transfer objects
│   │   │   ├── repository/        # JPA repositories
│   │   │   ├── mapper/            # MapStruct mappers
│   │   │   ├── exception/         # Custom exceptions
│   │   │   ├── util/              # Utility classes
│   │   │   └── PostServiceApplication.java
│   │   └── resources/
│   │       └── application.yml
│   └── test/
│       ├── java/com/inkwell/post/
│       │   └── service/impl/PostServiceImplTest.java
│       └── resources/
│           └── application.properties
└── pom.xml
```

## Entity Fields

### Post Entity
| Field | Type | Description |
|-------|------|-------------|
| postId | Integer | Primary key (auto-generated) |
| authorId | Integer | Foreign key to User service |
| title | String | Post title (max 255 chars) |
| slug | String | URL-safe slug (unique, auto-generated) |
| content | String | Rich HTML content |
| excerpt | String | Summary of the post (optional) |
| featuredImageUrl | String | URL to featured image (optional) |
| status | PostStatus | DRAFT, PUBLISHED, UNPUBLISHED, ARCHIVED |
| readTimeMin | Integer | Estimated reading time in minutes |
| viewCount | Integer | Total number of views |
| likesCount | Integer | Total number of likes |
| createdAt | LocalDateTime | Creation timestamp (immutable) |
| updatedAt | LocalDateTime | Last update timestamp (auto-updated) |
| publishedAt | LocalDateTime | Publication timestamp (set when published) |

## API Endpoints

### Create Post
```
POST /api/v1/posts
Content-Type: application/json

{
  "authorId": 1,
  "title": "My First Post",
  "content": "<p>Rich HTML content...</p>",
  "excerpt": "Optional summary",
  "featuredImageUrl": "https://example.com/image.jpg"
}

Response: 201 Created
{
  "postId": 1,
  "authorId": 1,
  "title": "My First Post",
  "slug": "my-first-post",
  "content": "...",
  "status": "DRAFT",
  "readTimeMin": 5,
  "viewCount": 0,
  "likesCount": 0,
  "createdAt": "2026-04-23T10:00:00",
  "updatedAt": "2026-04-23T10:00:00"
}
```

### Get Post by ID
```
GET /api/v1/posts/{postId}

Response: 200 OK
{...}
```

### Get Post by Slug
```
GET /api/v1/posts/slug/{slug}

Response: 200 OK
{...}
```

### Get All Posts
```
GET /api/v1/posts

Response: 200 OK
[{...}, {...}]
```

### Get Published Posts
```
GET /api/v1/posts/published

Response: 200 OK
[{...}, {...}]
```

### Get Posts by Author
```
GET /api/v1/posts/author/{authorId}

Response: 200 OK
[{...}, {...}]
```

### Get Posts by Author and Status
```
GET /api/v1/posts/author/{authorId}/status/{status}

Response: 200 OK
[{...}, {...}]
```

### Search Posts
```
GET /api/v1/posts/search?keyword=spring

Response: 200 OK
[{...}, {...}]
```

### Update Post
```
PUT /api/v1/posts/{postId}
Content-Type: application/json

{
  "title": "Updated Title",
  "content": "<p>Updated content...</p>",
  "status": "PUBLISHED"
}

Response: 200 OK
{...}
```

### Publish Post
```
POST /api/v1/posts/{postId}/publish

Response: 200 OK
{...}
```

### Unpublish Post
```
POST /api/v1/posts/{postId}/unpublish

Response: 200 OK
{...}
```

### Like Post
```
POST /api/v1/posts/{postId}/like

Response: 204 No Content
```

### Unlike Post
```
POST /api/v1/posts/{postId}/unlike

Response: 204 No Content
```

### Delete Post
```
DELETE /api/v1/posts/{postId}

Response: 204 No Content
```

## Running the Service

### Prerequisites
- Java 17+
- MySQL 8.0 running on localhost:3306
- Redis running on localhost:6379 (optional for basic testing)
- Maven 3.6+

### Setup Database
```sql
CREATE DATABASE IF NOT EXISTS inkwell_post;
USE inkwell_post;
```

### Build
```bash
mvn clean install
```

### Run
```bash
mvn spring-boot:run
```

The service will start on port 8082.

### Access Swagger UI
```
http://localhost:8082/swagger-ui.html
```

## Testing

### Run All Tests
```bash
mvn test
```

### Run Specific Test Class
```bash
mvn test -Dtest=PostServiceImplTest
```

### Test Coverage
The `PostServiceImplTest` class includes 20 comprehensive unit tests covering:
- Slug generation with various title formats
- Read time calculation (200 WPM baseline)
- CRUD operations
- Atomic increments for view and like counts
- Search functionality
- Post publication/unpublication
- Author-based filtering

## Key Design Decisions

### 1. Slug Generation
- **Automatic**: Generated from title on creation
- **Unique**: Enforced at database level with unique constraint
- **Format**: Lowercase, hyphenated, alphanumeric only
- **Benefit**: SEO-friendly URLs without manual input

### 2. Read Time Calculation
- **Baseline**: 200 words per minute (standard reading speed)
- **Calculation**: `(total_words / 200) rounded up`
- **Minimum**: 1 minute (even for very short posts)
- **Timing**: Calculated on creation and recalculated on content updates

### 3. Atomic Operations
- **View/Like Increments**: Use `@Modifying` JPA queries
- **Benefits**: 
  - Prevents race conditions during concurrent access
  - Ensures data consistency
  - No risk of lost updates

### 4. Post Status Management
- **DRAFT**: Initial state, not visible to public
- **PUBLISHED**: Visible to readers, publishedAt is set
- **UNPUBLISHED**: Was published, now hidden
- **ARCHIVED**: Legacy/old posts

## Error Handling

### Global Exception Handler
All exceptions are handled centrally with consistent error responses:

```json
{
  "timestamp": "2026-04-23T10:00:00",
  "status": 404,
  "error": "Post Not Found",
  "message": "Post not found with ID: 999",
  "path": "/api/v1/posts/999"
}
```

### Custom Exceptions
- `PostNotFoundException`: Thrown when post doesn't exist
- `IllegalArgumentException`: Thrown for duplicate slugs or invalid data

## Integration with Other Services

### Auth Service Integration
- Posts are associated with authors via `authorId`
- The service assumes author IDs are valid (no validation needed)
- Can be integrated with Auth Service for author details

### API Gateway
- All endpoints should be registered with API Gateway at `/api/v1/posts/**`

### Service Discovery
- Automatically registers with Eureka (port 8761)
- Service name: `post-service`

## Future Enhancements
- [ ] Implement Redis caching for frequently accessed posts
- [ ] Add pagination support
- [ ] Implement full-text search with Elasticsearch
- [ ] Add comment system
- [ ] Implement tag/category system
- [ ] Add revision history
- [ ] Implement soft deletes
- [ ] Add post scheduling (scheduled publishing)
- [ ] Add analytics (popular posts, trending topics)

## License
MIT License - InkWell Project

## Support
For issues or questions, please create an issue in the project repository.

