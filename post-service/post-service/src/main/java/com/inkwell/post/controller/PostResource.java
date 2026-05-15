package com.inkwell.post.controller;

import com.inkwell.post.dto.CreatePostRequest;
import com.inkwell.post.dto.PostDTO;
import com.inkwell.post.dto.UpdatePostRequest;
import com.inkwell.post.entity.PostStatus;
import com.inkwell.post.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Post API endpoints
 * Exposes CRUD operations and search functionality for posts
 */
@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Post Service", description = "API endpoints for managing blog posts")
public class PostResource {

    private final PostService postService;

    /**
     * Create a new post in DRAFT status
     *
     * @param createPostRequest the request containing post data
     * @return the created post with 201 Created status
     */
    @PostMapping
    @Operation(summary = "Create a new post", description = "Creates a new post in DRAFT status. Slug is auto-generated and read time is calculated.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Post created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request body")
    })
    public ResponseEntity<PostDTO> createPost(@Valid @RequestBody CreatePostRequest createPostRequest) {
        log.info("POST /api/v1/posts - Creating new post");
        PostDTO createdPost = postService.createPost(createPostRequest);
        return new ResponseEntity<>(createdPost, HttpStatus.CREATED);
    }

    /**
     * Get all posts
     *
     * @return list of all posts
     */
    @GetMapping
    @Operation(summary = "Get all posts", description = "Retrieves all posts including draft and published")
    @ApiResponse(responseCode = "200", description = "Posts retrieved successfully")
    public ResponseEntity<List<PostDTO>> getAllPosts() {
        log.info("GET /api/v1/posts - Fetching all posts");
        List<PostDTO> posts = postService.getAllPosts();
        return ResponseEntity.ok(posts);
    }

    /**
     * Get published posts only
     *
     * @return list of published posts
     */
    @GetMapping("/published")
    @Operation(summary = "Get published posts", description = "Retrieves only published posts")
    @ApiResponse(responseCode = "200", description = "Published posts retrieved successfully")
    public ResponseEntity<List<PostDTO>> getPublishedPosts() {
        log.info("GET /api/v1/posts/published - Fetching published posts");
        List<PostDTO> posts = postService.getPublishedPosts();
        return ResponseEntity.ok(posts);
    }

    /**
     * Get a post by ID
     *
     * @param postId the post ID
     * @return the post if found
     */
    @GetMapping("/{postId}")
    @Operation(summary = "Get post by ID", description = "Retrieves a specific post by its ID and increments view count")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Post retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Post not found")
    })
    public ResponseEntity<PostDTO> getPostById(
            @Parameter(description = "The post ID", example = "1")
            @PathVariable Integer postId,
            jakarta.servlet.http.HttpSession session) {
        log.info("GET /api/v1/posts/{} - Fetching post by ID", postId);
        PostDTO post = postService.getPostById(postId);
        // Increment view count per session
        postService.incrementViewCount(postId, session.getId());
        return ResponseEntity.ok(post);
    }

    /**
     * Get a post by slug
     *
     * @param slug the post slug
     * @return the post if found
     */
    @GetMapping("/slug/{slug}")
    @Operation(summary = "Get post by slug", description = "Retrieves a specific post by its unique slug")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Post retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Post not found")
    })
    public ResponseEntity<PostDTO> getPostBySlug(
            @Parameter(description = "The post slug", example = "my-first-post")
            @PathVariable String slug) {
        log.info("GET /api/v1/posts/slug/{} - Fetching post by slug", slug);
        PostDTO post = postService.getPostBySlug(slug);
        return ResponseEntity.ok(post);
    }

    /**
     * Get posts by author
     *
     * @param authorId the author ID
     * @return list of posts by the author
     */
    @GetMapping("/author/{authorId}")
    @Operation(summary = "Get posts by author", description = "Retrieves all posts by a specific author")
    @ApiResponse(responseCode = "200", description = "Author posts retrieved successfully")
    public ResponseEntity<List<PostDTO>> getPostsByAuthor(
            @Parameter(description = "The author ID", example = "1")
            @PathVariable Integer authorId) {
        log.info("GET /api/v1/posts/author/{} - Fetching posts by author", authorId);
        List<PostDTO> posts = postService.getPostsByAuthor(authorId);
        return ResponseEntity.ok(posts);
    }

    /**
     * Get posts by author and status
     *
     * @param authorId the author ID
     * @param status the post status
     * @return list of posts matching criteria
     */
    @GetMapping("/author/{authorId}/status/{status}")
    @Operation(summary = "Get posts by author and status", description = "Retrieves posts by author with a specific status")
    @ApiResponse(responseCode = "200", description = "Filtered posts retrieved successfully")
    public ResponseEntity<List<PostDTO>> getPostsByAuthorAndStatus(
            @Parameter(description = "The author ID", example = "1")
            @PathVariable Integer authorId,
            @Parameter(description = "The post status", example = "PUBLISHED")
            @PathVariable PostStatus status) {
        log.info("GET /api/v1/posts/author/{}/status/{} - Fetching posts by author and status", authorId, status);
        List<PostDTO> posts = postService.getPostsByAuthorAndStatus(authorId, status);
        return ResponseEntity.ok(posts);
    }

    /**
     * Search posts by keyword
     *
     * @param keyword the search keyword
     * @return list of matching posts
     */
    @GetMapping("/search")
    @Operation(summary = "Search posts", description = "Searches posts by keyword in title and content")
    @ApiResponse(responseCode = "200", description = "Search results retrieved successfully")
    public ResponseEntity<List<PostDTO>> searchPosts(
            @Parameter(description = "Search keyword", example = "Spring Boot")
            @RequestParam String keyword) {
        log.info("GET /api/v1/posts/search?keyword={} - Searching posts", keyword);
        List<PostDTO> posts = postService.searchPosts(keyword);
        return ResponseEntity.ok(posts);
    }

    /**
     * Update a post
     *
     * @param postId the post ID
     * @param updatePostRequest the update request
     * @return the updated post
     */
    @PutMapping("/{postId}")
    @Operation(summary = "Update a post", description = "Updates an existing post. Slug is regenerated if title changes, read time is recalculated if content changes")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Post updated successfully"),
        @ApiResponse(responseCode = "404", description = "Post not found")
    })
    public ResponseEntity<PostDTO> updatePost(
            @Parameter(description = "The post ID", example = "1")
            @PathVariable Integer postId,
            @Valid @RequestBody UpdatePostRequest updatePostRequest) {
        log.info("PUT /api/v1/posts/{} - Updating post", postId);
        PostDTO updatedPost = postService.updatePost(postId, updatePostRequest);
        return ResponseEntity.ok(updatedPost);
    }

    /**
     * Publish a post
     *
     * @param postId the post ID
     * @return the published post
     */
    @PostMapping("/{postId}/publish")
    @Operation(summary = "Publish a post", description = "Changes post status from DRAFT to PUBLISHED and sets publishedAt timestamp")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Post published successfully"),
        @ApiResponse(responseCode = "404", description = "Post not found")
    })
    public ResponseEntity<PostDTO> publishPost(
            @Parameter(description = "The post ID", example = "1")
            @PathVariable Integer postId) {
        log.info("POST /api/v1/posts/{}/publish - Publishing post", postId);
        PostDTO publishedPost = postService.publishPost(postId);
        return ResponseEntity.ok(publishedPost);
    }

    /**
     * Unpublish a post
     *
     * @param postId the post ID
     * @return the unpublished post
     */
    @PostMapping("/{postId}/unpublish")
    @Operation(summary = "Unpublish a post", description = "Changes post status to UNPUBLISHED")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Post unpublished successfully"),
        @ApiResponse(responseCode = "404", description = "Post not found")
    })
    public ResponseEntity<PostDTO> unpublishPost(
            @Parameter(description = "The post ID", example = "1")
            @PathVariable Integer postId) {
        log.info("POST /api/v1/posts/{}/unpublish - Unpublishing post", postId);
        PostDTO unpublishedPost = postService.unpublishPost(postId);
        return ResponseEntity.ok(unpublishedPost);
    }

    /**
     * Feature (pin) or unfeature a post
     *
     * @param postId the post ID
     * @param isFeatured whether to feature the post
     * @return the updated post
     */
    @PostMapping("/{postId}/feature")
    @Operation(summary = "Feature or unfeature a post", description = "Pins a post to the top of the feed or unpins it (Admin only)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Post feature status updated successfully"),
        @ApiResponse(responseCode = "404", description = "Post not found")
    })
    public ResponseEntity<PostDTO> featurePost(
            @Parameter(description = "The post ID", example = "1")
            @PathVariable Integer postId,
            @Parameter(description = "Whether to feature the post", example = "true")
            @RequestParam boolean isFeatured) {
        log.info("POST /api/v1/posts/{}/feature?isFeatured={} - Updating feature status", postId, isFeatured);
        PostDTO updatedPost = postService.featurePost(postId, isFeatured);
        return ResponseEntity.ok(updatedPost);
    }

    /**
     * Like a post
     *
     * @param postId the post ID
     * @return response indicating success
     */
    @PostMapping("/{postId}/like")
    @Operation(summary = "Like a post", description = "Increments the likes count for a post")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Post liked successfully"),
        @ApiResponse(responseCode = "404", description = "Post not found")
    })
    public ResponseEntity<Void> likePost(
            @Parameter(description = "The post ID", example = "1")
            @PathVariable Integer postId,
            @RequestHeader(value = "X-User-Id", required = false) Integer userId) {
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        log.info("POST /api/v1/posts/{}/like - Liking post for user {}", postId, userId);
        boolean created = postService.likePost(postId, userId);
        return created
                ? ResponseEntity.status(HttpStatus.CREATED).build()
                : ResponseEntity.noContent().build();
    }

    /**
     * Unlike a post
     *
     * @param postId the post ID
     * @return response indicating success
     */
    @PostMapping("/{postId}/unlike")
    @Operation(summary = "Unlike a post", description = "Decrements the likes count for a post")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Post unliked successfully"),
        @ApiResponse(responseCode = "404", description = "Post not found")
    })
    public ResponseEntity<Void> unlikePost(
            @Parameter(description = "The post ID", example = "1")
            @PathVariable Integer postId,
            @RequestHeader(value = "X-User-Id", required = false) Integer userId) {
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        log.info("POST /api/v1/posts/{}/unlike - Unliking post for user {}", postId, userId);
        postService.unlikePost(postId, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{postId}/view")
    @Operation(summary = "Increment view count", description = "Increments the view count for a post manually")
    public ResponseEntity<Void> incrementViewCount(
            @PathVariable Integer postId,
            jakarta.servlet.http.HttpSession session) {
        postService.incrementViewCount(postId, session.getId());
        return ResponseEntity.noContent().build();
    }

    /**
     * Delete a post
     *
     * @param postId the post ID
     * @return response indicating success
     */
    @DeleteMapping("/{postId}")
    @Operation(summary = "Delete a post", description = "Permanently deletes a post")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Post deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Post not found")
    })
    public ResponseEntity<Void> deletePost(
            @Parameter(description = "The post ID", example = "1")
            @PathVariable Integer postId) {
        log.info("DELETE /api/v1/posts/{} - Deleting post", postId);
        postService.deletePost(postId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/author/{authorId}")
    @Operation(summary = "Delete all posts by author", description = "Permanently deletes every post owned by an author")
    public ResponseEntity<Void> deletePostsByAuthor(
            @Parameter(description = "The author ID", example = "1")
            @PathVariable Integer authorId) {
        log.info("DELETE /api/v1/posts/author/{} - Deleting all posts by author", authorId);
        postService.deletePostsByAuthor(authorId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/analytics/most-viewed")
    @Operation(summary = "Get most viewed posts", description = "Returns a list of the most viewed posts on the platform")
    public ResponseEntity<List<PostDTO>> getMostViewedPosts(@RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(postService.getMostViewedPosts(limit));
    }

    @GetMapping("/analytics/most-active-authors")
    @Operation(summary = "Get most active authors", description = "Returns a list of authors with the highest post count")
    public ResponseEntity<List<com.inkwell.post.dto.AuthorAnalyticsDTO>> getMostActiveAuthors(@RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(postService.getMostActiveAuthors(limit));
    }

    @GetMapping("/analytics/trending-tags")
    @Operation(summary = "Get trending tags", description = "Returns tags sorted by their usage in posts")
    public ResponseEntity<List<com.inkwell.post.dto.TagDTO>> getTrendingTags(@RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(postService.getTrendingTags(limit));
    }
}

