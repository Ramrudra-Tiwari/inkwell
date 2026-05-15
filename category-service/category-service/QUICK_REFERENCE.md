# Category & Tag Service - Quick Reference

## API Base URL
```
http://localhost:8084/api/v1
```

## Categories API

### Create Category
```bash
POST /api/v1/categories
Content-Type: application/json

{
  "name": "Programming",
  "description": "Programming tutorials and guides",
  "parentCategoryId": null
}
```

**Response (201):**
```json
{
  "categoryId": 1,
  "name": "Programming",
  "slug": "programming",
  "description": "Programming tutorials and guides",
  "parentCategoryId": null,
  "postCount": 0,
  "createdAt": "2026-04-23T20:31:25+05:30",
  "updatedAt": "2026-04-23T20:31:25+05:30"
}
```

### Create Child Category
```bash
POST /api/v1/categories
Content-Type: application/json

{
  "name": "Java Programming",
  "description": "Java tutorials",
  "parentCategoryId": 1
}
```

### Get Category by ID
```bash
GET /api/v1/categories/1
```

### Get Category by Slug
```bash
GET /api/v1/categories/slug/programming
```

### Get All Categories
```bash
GET /api/v1/categories
```

### Get Top-Level Categories
```bash
GET /api/v1/categories/top-level
```

### Get Child Categories
```bash
GET /api/v1/categories/1/children
```

### Update Category
```bash
PUT /api/v1/categories/1
Content-Type: application/json

{
  "name": "Web Development",
  "description": "Updated description",
  "parentCategoryId": null
}
```

### Delete Category
```bash
DELETE /api/v1/categories/1
```

### Search Categories
```bash
GET /api/v1/categories/search?name=program
```

### Assign Post to Category
```bash
POST /api/v1/categories/1/posts
Content-Type: application/json

{
  "postId": 100
}
```

### Remove Post from Category
```bash
DELETE /api/v1/categories/1/posts/100
```

## Tags API

### Create Tag
```bash
POST /api/v1/tags
Content-Type: application/json

{
  "name": "Spring Boot"
}
```

**Response (201):**
```json
{
  "tagId": 1,
  "name": "Spring Boot",
  "slug": "spring-boot",
  "postCount": 0,
  "createdAt": "2026-04-23T20:31:25+05:30",
  "updatedAt": "2026-04-23T20:31:25+05:30"
}
```

### Get Tag by ID
```bash
GET /api/v1/tags/1
```

### Get Tag by Slug
```bash
GET /api/v1/tags/slug/spring-boot
```

### Get All Tags
```bash
GET /api/v1/tags
```

### Get Trending Tags
```bash
GET /api/v1/tags/trending?limit=10
```

### Update Tag
```bash
PUT /api/v1/tags/1
Content-Type: application/json

{
  "name": "Spring Framework"
}
```

### Delete Tag
```bash
DELETE /api/v1/tags/1
```

### Search Tags
```bash
GET /api/v1/tags/search?name=spring
```

### Assign Post to Tag
```bash
POST /api/v1/tags/1/posts
Content-Type: application/json

{
  "postId": 100
}
```

### Remove Post from Tag
```bash
DELETE /api/v1/tags/1/posts/100
```

## Error Responses

### 404 Not Found
```json
{
  "timestamp": "2026-04-23T20:31:40.389+05:30",
  "status": 404,
  "error": "Taxonomy Not Found",
  "message": "Category not found with ID: 123",
  "path": "/api/v1/categories/123"
}
```

### 400 Bad Request
```json
{
  "timestamp": "2026-04-23T20:31:40.389+05:30",
  "status": 400,
  "error": "Invalid Argument",
  "message": "Category slug already exists: programming",
  "path": "/api/v1/categories"
}
```

### 409 Conflict (Validation)
```json
{
  "timestamp": "2026-04-23T20:31:40.389+05:30",
  "status": 400,
  "error": "Validation Failed",
  "message": "Invalid request parameters",
  "details": [
    "name: Name is required"
  ],
  "path": "/api/v1/categories"
}
```

## Data Models

### TaxonomyRequest
```json
{
  "name": "string (required)",
  "description": "string (optional, categories only)",
  "parentCategoryId": "integer (optional, categories only)"
}
```

### CategoryDTO
```json
{
  "categoryId": "integer",
  "name": "string",
  "slug": "string",
  "description": "string",
  "parentCategoryId": "integer",
  "postCount": "integer",
  "createdAt": "datetime",
  "updatedAt": "datetime"
}
```

### TagDTO
```json
{
  "tagId": "integer",
  "name": "string",
  "slug": "string",
  "postCount": "integer",
  "createdAt": "datetime",
  "updatedAt": "datetime"
}
```

### PostAssociationRequest
```json
{
  "postId": "integer (required)",
  "categoryId": "integer (optional)",
  "tagId": "integer (optional)"
}
```

## Service Commands

### Start Service
```bash
cd inkwell/category-service
./mvnw spring-boot:run
```

### Run Tests
```bash
./mvnw test
```

### Compile Only
```bash
./mvnw clean compile
```

### Build JAR
```bash
./mvnw clean package
```

## Database Commands

### Create Database
```sql
CREATE DATABASE inkwell_category;
```

### Check Tables
```sql
USE inkwell_category;
SHOW TABLES;
DESCRIBE categories;
DESCRIBE tags;
```

## Health Checks

### Service Health
```bash
GET http://localhost:8084/actuator/health
```

### Swagger Documentation
```bash
http://localhost:8084/swagger-ui.html
```

## Common Issues

### Database Connection
- Ensure MySQL is running on port 3306
- Verify database `inkwell_category` exists
- Check credentials in `application.yml`

### Port Conflicts
- Default port: 8084
- Change in `application.yml` if needed

### Eureka Registration
- Ensure Discovery Server is running on port 8761
- Check service registration at `http://localhost:8761`

## Integration Examples

### Frontend Integration
```javascript
// Create category
const response = await fetch('/api/v1/categories', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    name: 'Technology',
    description: 'Tech articles'
  })
});
```

### Post Service Integration
```javascript
// When creating a post, associate with categories/tags
await fetch('/api/v1/categories/1/posts', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ postId: 123 })
});
```

## Performance Tips

- Use slugs for URL-friendly category/tag access
- Implement caching for frequently accessed categories
- Batch post associations when possible
- Monitor post count updates for performance
