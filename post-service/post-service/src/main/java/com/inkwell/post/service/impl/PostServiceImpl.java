package com.inkwell.post.service.impl;

import com.inkwell.post.dto.CreatePostRequest;
import com.inkwell.post.dto.PostDTO;
import com.inkwell.post.dto.UpdatePostRequest;
import com.inkwell.post.entity.Post;
import com.inkwell.post.entity.PostLike;
import com.inkwell.post.entity.PostStatus;
import com.inkwell.post.exception.PostNotFoundException;
import com.inkwell.post.mapper.PostMapper;
import com.inkwell.post.repository.PostLikeRepository;
import com.inkwell.post.repository.PostRepository;
import com.inkwell.post.service.PostService;
import com.inkwell.post.util.SlugUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of PostService
 * Contains business logic for post operations including:
 * - Slugification: Convert titles to URL-safe slugs
 * - Read Time Calculation: Calculate reading time based on word count (200 WPM)
 * - Atomic Increments: Use @Modifying queries to prevent race conditions
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final PostMapper postMapper;
    private final com.inkwell.post.repository.CategoryRepository categoryRepository;
    private final com.inkwell.post.repository.TagRepository tagRepository;
    private final com.inkwell.post.client.AuthClient authClient;
    private final com.inkwell.post.client.MessagingClient messagingClient;
    private final com.inkwell.post.client.CommentClient commentClient;
    private final com.inkwell.post.repository.AuditLogRepository auditLogRepository;
    private final com.inkwell.post.repository.PostViewRepository postViewRepository;
    private final PostLikeRepository postLikeRepository;
    private final com.inkwell.post.client.NewsletterClient newsletterClient;

    private void logAdminAction(String action, String target, String performedBy, String details) {
        com.inkwell.post.entity.AuditLog logEntry = com.inkwell.post.entity.AuditLog.builder()
                .action(action)
                .target(target)
                .performedBy(performedBy)
                .details(details)
                .build();
        auditLogRepository.save(logEntry);
    }

    /**
     * Create a new post
     * - Generates slug from title
     * - Calculates read time from content
     *
     * @param createPostRequest the request containing post data
     * @return the created PostDTO
     */
    @Override
    public PostDTO createPost(CreatePostRequest createPostRequest) {
        log.info("Creating new post with title: {}", createPostRequest.getTitle());

        // Generate slug from title
        String slug = SlugUtil.generateSlug(createPostRequest.getTitle());

        // Check if slug already exists
        if (postRepository.existsBySlug(slug)) {
            log.warn("Slug already exists: {}", slug);
            throw new IllegalArgumentException("A post with a similar title already exists. Please use a different title.");
        }

        // Calculate read time (200 words per minute)
        int readTimeMin = calculateReadTime(createPostRequest.getContent());

        PostStatus requestedStatus = createPostRequest.getStatus() != null
                ? createPostRequest.getStatus()
                : PostStatus.DRAFT;

        Post post = Post.builder()
                .authorId(createPostRequest.getAuthorId())
                .title(createPostRequest.getTitle())
                .slug(slug)
                .content(createPostRequest.getContent())
                .excerpt(createPostRequest.getExcerpt())
                .featuredImageUrl(createPostRequest.getFeaturedImageUrl())
                .status(requestedStatus)
                .publishedAt(requestedStatus == PostStatus.PUBLISHED ? LocalDateTime.now() : null)
                .readTimeMin(readTimeMin)
                .viewCount(0)
                .likesCount(0)
                .build();

        // Set Category
        if (createPostRequest.getCategoryId() != null) {
            categoryRepository.findById(createPostRequest.getCategoryId())
                    .ifPresent(post::setCategory);
        }

        // Set Tags
        if (createPostRequest.getTagIds() != null && !createPostRequest.getTagIds().isEmpty()) {
            java.util.List<com.inkwell.post.entity.Tag> tags = tagRepository.findAllById(createPostRequest.getTagIds());
            post.setTags(new java.util.HashSet<>(tags));
        }

        post = postRepository.save(post);
        log.info("Post created successfully with ID: {} and slug: {}", post.getPostId(), slug);

        if (post.getStatus() == PostStatus.PUBLISHED) {
            notifyFollowers(post);
            newsletterClient.triggerNewsletterForNewPost(post.getTitle(), post.getExcerpt(), post.getPostId());
        }

        return postMapper.toDTO(post);
    }

    /**
     * Retrieve a post by ID
     *
     * @param postId the post ID
     * @return the PostDTO if found
     * @throws PostNotFoundException if not found
     */
    @Override
    @Transactional(readOnly = true)
    public PostDTO getPostById(Integer postId) {
        log.debug("Fetching post by ID: {}", postId);
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> {
                    log.error("Post not found with ID: {}", postId);
                    return new PostNotFoundException("Post not found with ID: " + postId);
                });
        return populateExtraFields(postMapper.toDTO(post));
    }

    private PostDTO populateExtraFields(PostDTO dto) {
        if (dto != null) {
            dto.setCommentCount(commentClient.getCommentCountByPostId(dto.getPostId()));
        }
        return dto;
    }

    /**
     * Retrieve a post by its slug
     *
     * @param slug the post slug
     * @return the PostDTO if found
     * @throws PostNotFoundException if not found
     */
    @Override
    @Transactional(readOnly = true)
    public PostDTO getPostBySlug(String slug) {
        log.debug("Fetching post by slug: {}", slug);
        Post post = postRepository.findBySlug(slug)
                .orElseThrow(() -> {
                    log.error("Post not found with slug: {}", slug);
                    return new PostNotFoundException("Post not found with slug: " + slug);
                });
        return populateExtraFields(postMapper.toDTO(post));
    }

    /**
     * Get all posts
     *
     * @return list of PostDTOs
     */
    @Override
    @Transactional(readOnly = true)
    public List<PostDTO> getAllPosts() {
        log.debug("Fetching all posts");
        return postRepository.findAll()
                .stream()
                .map(postMapper::toDTO)
                .map(this::populateExtraFields)
                .collect(Collectors.toList());
    }

    /**
     * Get published posts only
     *
     * @return list of published PostDTOs
     */
    @Override
    @Transactional(readOnly = true)
    public List<PostDTO> getPublishedPosts() {
        log.debug("Fetching published posts");
        return postRepository.findAllPublishedPosts()
                .stream()
                .map(postMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get all posts by a specific author
     *
     * @param authorId the author ID
     * @return list of PostDTOs by the author
     */
    @Override
    @Transactional(readOnly = true)
    public List<PostDTO> getPostsByAuthor(Integer authorId) {
        log.debug("Fetching posts by author ID: {}", authorId);
        return postRepository.findByAuthorId(authorId)
                .stream()
                .map(postMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get all posts by a specific author with a given status
     *
     * @param authorId the author ID
     * @param status the post status
     * @return list of PostDTOs
     */
    @Override
    @Transactional(readOnly = true)
    public List<PostDTO> getPostsByAuthorAndStatus(Integer authorId, PostStatus status) {
        log.debug("Fetching posts by author ID: {} and status: {}", authorId, status);
        return postRepository.findByAuthorIdAndStatus(authorId, status)
                .stream()
                .map(postMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Search posts by keyword in title and content
     *
     * @param keyword the search keyword
     * @return list of matching PostDTOs
     */
    @Override
    @Transactional(readOnly = true)
    public List<PostDTO> searchPosts(String keyword) {
        log.info("Searching posts with keyword: {}", keyword);
        if (keyword == null || keyword.trim().isEmpty()) {
            log.warn("Search keyword is empty");
            return List.of();
        }
        return postRepository.searchByKeyword(keyword)
                .stream()
                .map(postMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Update an existing post
     * - Regenerates slug if title is changed
     * - Recalculates read time if content is changed
     *
     * @param postId the post ID
     * @param updatePostRequest the update request
     * @return the updated PostDTO
     * @throws PostNotFoundException if not found
     */
    @Override
    public PostDTO updatePost(Integer postId, UpdatePostRequest updatePostRequest) {
        log.info("Updating post with ID: {}", postId);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> {
                    log.error("Post not found with ID: {}", postId);
                    return new PostNotFoundException("Post not found with ID: " + postId);
                });

        // Update title and regenerate slug if title changed
        if (updatePostRequest.getTitle() != null && !updatePostRequest.getTitle().equals(post.getTitle())) {
            String newSlug = SlugUtil.generateSlug(updatePostRequest.getTitle());
            if (!newSlug.equals(post.getSlug()) && postRepository.existsBySlug(newSlug)) {
                throw new IllegalArgumentException("A post with a similar title already exists. Please use a different title.");
            }
            post.setTitle(updatePostRequest.getTitle());
            post.setSlug(newSlug);
        }

        // Update content and recalculate read time if content changed
        if (updatePostRequest.getContent() != null && !updatePostRequest.getContent().equals(post.getContent())) {
            post.setContent(updatePostRequest.getContent());
            post.setReadTimeMin(calculateReadTime(updatePostRequest.getContent()));
        }

        // Update excerpt if provided
        if (updatePostRequest.getExcerpt() != null) {
            post.setExcerpt(updatePostRequest.getExcerpt());
        }

        // Update featured image if provided
        if (updatePostRequest.getFeaturedImageUrl() != null) {
            post.setFeaturedImageUrl(updatePostRequest.getFeaturedImageUrl());
        }

        // Update status if provided
        if (updatePostRequest.getStatus() != null) {
            PostStatus oldStatus = post.getStatus();
            post.setStatus(updatePostRequest.getStatus());

            // Set publishedAt when publishing
            if (oldStatus != PostStatus.PUBLISHED && updatePostRequest.getStatus() == PostStatus.PUBLISHED) {
                post.setPublishedAt(LocalDateTime.now());
                log.info("Post published at: {}", post.getPublishedAt());
                notifyFollowers(post);
            }
        }

        // Update Category
        if (updatePostRequest.getCategoryId() != null) {
            categoryRepository.findById(updatePostRequest.getCategoryId())
                    .ifPresent(post::setCategory);
        }

        // Update Tags
        if (updatePostRequest.getTagIds() != null) {
            java.util.List<com.inkwell.post.entity.Tag> tags = tagRepository.findAllById(updatePostRequest.getTagIds());
            post.setTags(new java.util.HashSet<>(tags));
        }

        post = postRepository.save(post);
        log.info("Post updated successfully with ID: {}", postId);

        return postMapper.toDTO(post);
    }

    /**
     * Delete a post
     *
     * @param postId the post ID
     * @throws PostNotFoundException if not found
     */
    @Override
    public void deletePost(Integer postId) {
        log.info("Deleting post with ID: {}", postId);

        if (!postRepository.existsById(postId)) {
            log.error("Post not found with ID: {}", postId);
            throw new PostNotFoundException("Post not found with ID: " + postId);
        }

        deletePostAssociations(postId);
        postRepository.deleteById(postId);
        
        // Requirement 2.4: Remove all associated comments
        commentClient.deleteCommentsByPostId(postId);

        logAdminAction("PURGE_POST", "Post ID: " + postId, "ADMIN", "Post and all its comments permanently deleted.");
        log.info("Post deleted successfully with ID: {}", postId);
    }

    @Override
    @Transactional
    public void deletePostsByAuthor(Integer authorId) {
        log.info("Deleting all posts for author ID: {}", authorId);

        List<Post> posts = postRepository.findByAuthorId(authorId);
        posts.forEach(post -> {
            deletePostAssociations(post.getPostId());
            postRepository.deleteById(post.getPostId());
            commentClient.deleteCommentsByPostId(post.getPostId());
        });

        logAdminAction(
                "PURGE_AUTHOR_POSTS",
                "Author ID: " + authorId,
                "ADMIN",
                "Deleted " + posts.size() + " post(s) and their comments for expelled author."
        );
        log.info("Deleted {} post(s) for author ID: {}", posts.size(), authorId);
    }

    private void deletePostAssociations(Integer postId) {
        postLikeRepository.deleteByPostId(postId);
        postViewRepository.deleteByPostId(postId);
        postRepository.deleteTagLinksByPostId(postId);
    }

    /**
     * Increment view count for a post (called when post is viewed)
     * Uses atomic operation to prevent race conditions
     *
     * @param postId the post ID
     */
    @Override
    public void incrementViewCount(Integer postId, String sessionId) {
        log.debug("Incrementing view count for post ID: {} with session: {}", postId, sessionId);
        
        if (!postRepository.existsById(postId)) {
            throw new PostNotFoundException("Post not found with ID: " + postId);
        }

        // Check if this session has already viewed this post
        if (!postViewRepository.existsByPostIdAndSessionId(postId, sessionId)) {
            // Save the view
            com.inkwell.post.entity.PostView view = com.inkwell.post.entity.PostView.builder()
                    .postId(postId)
                    .sessionId(sessionId)
                    .build();
            postViewRepository.save(view);
            
            // Atomically increment the count
            postRepository.incrementViewCount(postId);
            log.info("View count incremented for post ID: {} (First time for session: {})", postId, sessionId);
        } else {
            log.debug("View count NOT incremented for post ID: {} (Session {} already viewed)", postId, sessionId);
        }
    }

    /**
     * Increment likes count for a post
     * Uses atomic operation to prevent race conditions
     *
     * @param postId the post ID
     */
    @Override
    public void incrementLikesCount(Integer postId) {
        log.debug("Incrementing likes count for post ID: {}", postId);
        if (!postRepository.existsById(postId)) {
            throw new PostNotFoundException("Post not found with ID: " + postId);
        }
        postRepository.incrementLikesCount(postId);
    }

    @Override
    public boolean likePost(Integer postId, Integer userId) {
        log.debug("Liking post ID: {} for user ID: {}", postId, userId);
        if (!postRepository.existsById(postId)) {
            throw new PostNotFoundException("Post not found with ID: " + postId);
        }

        if (postLikeRepository.existsByPostIdAndUserId(postId, userId)) {
            return false;
        }

        postLikeRepository.save(PostLike.builder()
                .postId(postId)
                .userId(userId)
                .build());
        postRepository.incrementLikesCount(postId);
        return true;
    }

    @Override
    public boolean unlikePost(Integer postId, Integer userId) {
        log.debug("Unliking post ID: {} for user ID: {}", postId, userId);
        if (!postRepository.existsById(postId)) {
            throw new PostNotFoundException("Post not found with ID: " + postId);
        }

        return postLikeRepository.findByPostIdAndUserId(postId, userId)
                .map(like -> {
                    postLikeRepository.delete(like);
                    postRepository.decrementLikesCount(postId);
                    return true;
                })
                .orElse(false);
    }

    /**
     * Decrement likes count for a post (when user unlikes)
     * Uses atomic operation to prevent race conditions
     *
     * @param postId the post ID
     */
    @Override
    public void decrementLikesCount(Integer postId) {
        log.debug("Decrementing likes count for post ID: {}", postId);
        if (!postRepository.existsById(postId)) {
            throw new PostNotFoundException("Post not found with ID: " + postId);
        }
        postRepository.decrementLikesCount(postId);
    }

    /**
     * Publish a post (change status from DRAFT to PUBLISHED)
     *
     * @param postId the post ID
     * @return the updated PostDTO
     * @throws PostNotFoundException if not found
     */
    @Override
    public PostDTO publishPost(Integer postId) {
        log.info("Publishing post with ID: {}", postId);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> {
                    log.error("Post not found with ID: {}", postId);
                    return new PostNotFoundException("Post not found with ID: " + postId);
                });

        post.setStatus(PostStatus.PUBLISHED);
        post.setPublishedAt(LocalDateTime.now());
        post = postRepository.save(post);

        notifyFollowers(post);
        
        // Requirement 2.8: Automatic newsletter for active subscribers
        newsletterClient.triggerNewsletterForNewPost(post.getTitle(), post.getExcerpt(), post.getPostId());

        log.info("Post published successfully with ID: {}", postId);
        return postMapper.toDTO(post);
    }

    /**
     * Unpublish a post
     *
     * @param postId the post ID
     * @return the updated PostDTO
     * @throws PostNotFoundException if not found
     */
    @Override
    public PostDTO unpublishPost(Integer postId) {
        log.info("Unpublishing post with ID: {}", postId);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> {
                    log.error("Post not found with ID: {}", postId);
                    return new PostNotFoundException("Post not found with ID: " + postId);
                });

        post.setStatus(PostStatus.UNPUBLISHED);
        post = postRepository.save(post);

        log.info("Post unpublished successfully with ID: {}", postId);
        return postMapper.toDTO(post);
    }

    /**
     * Calculate read time based on word count
     * Assumes 200 words per minute (WPM)
     * Minimum 1 minute
     *
     * @param content the post content
     * @return read time in minutes
     */
    private int calculateReadTime(String content) {
        if (content == null || content.trim().isEmpty()) {
            return 1;
        }

        // Remove HTML tags for accurate word count
        String plainText = content.replaceAll("<[^>]*>", "");

        // Count words (split by whitespace)
        String[] words = plainText.trim().split("\\s+");
        int wordCount = words.length;

        // Calculate read time: 200 WPM
        int readTime = Math.max(1, Math.round(wordCount / 200f));

        log.debug("Calculated read time: {} minutes for {} words", readTime, wordCount);
        return readTime;
    }

    @Override
    public PostDTO featurePost(Integer postId, boolean isFeatured) {
        log.info("Setting feature status for post {} to {}", postId, isFeatured);
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("Post not found with ID: " + postId));
        
        post.setFeatured(isFeatured);
        post = postRepository.save(post);
        
        logAdminAction(isFeatured ? "PIN_POST" : "UNPIN_POST", "Post ID: " + postId, "ADMIN", 
                isFeatured ? "Post pinned to top of feed." : "Post unpinned from top of feed.");
        
        return postMapper.toDTO(post);
    }

    @Override
    public List<PostDTO> getMostViewedPosts(int limit) {
        return postRepository.findTopMostViewedPosts(org.springframework.data.domain.PageRequest.of(0, limit))
                .stream()
                .map(postMapper::toDTO)
                .map(this::populateExtraFields)
                .collect(Collectors.toList());
    }

    @Override
    public List<com.inkwell.post.dto.AuthorAnalyticsDTO> getMostActiveAuthors(int limit) {
        return postRepository.findMostActiveAuthors(org.springframework.data.domain.PageRequest.of(0, limit))
                .stream()
                .map(obj -> new com.inkwell.post.dto.AuthorAnalyticsDTO((Integer)obj[0], (Long)obj[1]))
                .collect(Collectors.toList());
    }

    @Override
    public List<com.inkwell.post.dto.TagDTO> getTrendingTags(int limit) {
        return tagRepository.findTrendingTags(org.springframework.data.domain.PageRequest.of(0, limit))
                .stream()
                .map(tag -> com.inkwell.post.dto.TagDTO.builder()
                        .tagId(tag.getTagId())
                        .name(tag.getName())
                        .slug(tag.getSlug())
                        .build())
                .collect(Collectors.toList());
    }

    private void notifyFollowers(Post post) {
        try {
            log.info("Notifying followers about new post: {}", post.getPostId());
            java.util.List<Integer> followerIds = authClient.getFollowerIds(post.getAuthorId());
            
            if (followerIds != null && !followerIds.isEmpty()) {
                String message = String.format("A writer you follow just published: %s", post.getTitle());
                for (Integer followerId : followerIds) {
                    messagingClient.sendNotification(
                        followerId,
                        post.getAuthorId(),
                        "NEW_POST",
                        "New Story Published",
                        message
                    );
                }
                log.info("Successfully sent notifications to {} followers", followerIds.size());
            }
        } catch (Exception e) {
            log.error("Failed to notify followers for post: {}", post.getPostId(), e);
        }
    }
}

