package com.inkwell.post.service;

import com.inkwell.post.dto.CreatePostRequest;
import com.inkwell.post.dto.PostDTO;
import com.inkwell.post.dto.UpdatePostRequest;
import com.inkwell.post.entity.PostStatus;

import java.util.List;

/**
 * Service interface for Post operations
 * Defines contract for post-related business logic
 */
public interface PostService {

    /**
     * Create a new post in DRAFT status
     *
     * @param createPostRequest the request containing post data
     * @return the created PostDTO
     */
    PostDTO createPost(CreatePostRequest createPostRequest);

    /**
     * Retrieve a post by ID
     *
     * @param postId the post ID
     * @return the PostDTO if found
     * @throws com.inkwell.post.exception.PostNotFoundException if not found
     */
    PostDTO getPostById(Integer postId);

    /**
     * Retrieve a post by its slug
     *
     * @param slug the post slug
     * @return the PostDTO if found
     * @throws com.inkwell.post.exception.PostNotFoundException if not found
     */
    PostDTO getPostBySlug(String slug);

    /**
     * Get all posts (paginated or not, depending on implementation)
     *
     * @return list of PostDTOs
     */
    List<PostDTO> getAllPosts();

    /**
     * Get published posts only
     *
     * @return list of published PostDTOs
     */
    List<PostDTO> getPublishedPosts();

    /**
     * Get all posts by a specific author
     *
     * @param authorId the author ID
     * @return list of PostDTOs by the author
     */
    List<PostDTO> getPostsByAuthor(Integer authorId);

    /**
     * Get all posts by a specific author with a given status
     *
     * @param authorId the author ID
     * @param status the post status
     * @return list of PostDTOs
     */
    List<PostDTO> getPostsByAuthorAndStatus(Integer authorId, PostStatus status);

    /**
     * Search posts by keyword
     *
     * @param keyword the search keyword
     * @return list of matching PostDTOs
     */
    List<PostDTO> searchPosts(String keyword);

    /**
     * Update an existing post
     *
     * @param postId the post ID
     * @param updatePostRequest the update request
     * @return the updated PostDTO
     * @throws com.inkwell.post.exception.PostNotFoundException if not found
     */
    PostDTO updatePost(Integer postId, UpdatePostRequest updatePostRequest);

    /**
     * Delete a post
     *
     * @param postId the post ID
     * @throws com.inkwell.post.exception.PostNotFoundException if not found
     */
    void deletePost(Integer postId);

    /**
     * Delete all posts owned by an author.
     *
     * @param authorId the author ID
     */
    void deletePostsByAuthor(Integer authorId);

    /**
     * Increment view count for a post (called when post is viewed)
     * Once per unique session.
     *
     * @param postId the post ID
     * @param sessionId the unique session ID of the viewer
     */
    void incrementViewCount(Integer postId, String sessionId);

    /**
     * Increment likes count for a post
     *
     * @param postId the post ID
     */
    void incrementLikesCount(Integer postId);

    boolean likePost(Integer postId, Integer userId);

    boolean unlikePost(Integer postId, Integer userId);

    /**
     * Decrement likes count for a post (when user unlikes)
     *
     * @param postId the post ID
     */
    void decrementLikesCount(Integer postId);

    /**
     * Publish a post (change status from DRAFT to PUBLISHED)
     *
     * @param postId the post ID
     * @return the updated PostDTO
     * @throws com.inkwell.post.exception.PostNotFoundException if not found
     */
    PostDTO publishPost(Integer postId);

    /**
     * Unpublish a post
     *
     * @param postId the post ID
     * @return the updated PostDTO
     * @throws com.inkwell.post.exception.PostNotFoundException if not found
     */
    PostDTO unpublishPost(Integer postId);

    /**
     * Feature (pin) or unfeature a post
     *
     * @param postId the post ID
     * @param isFeatured whether to feature the post
     * @return the updated PostDTO
     */
    PostDTO featurePost(Integer postId, boolean isFeatured);

    List<PostDTO> getMostViewedPosts(int limit);

    List<com.inkwell.post.dto.AuthorAnalyticsDTO> getMostActiveAuthors(int limit);

    List<com.inkwell.post.dto.TagDTO> getTrendingTags(int limit);
}

