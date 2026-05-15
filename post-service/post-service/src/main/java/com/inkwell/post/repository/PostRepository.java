package com.inkwell.post.repository;

import com.inkwell.post.entity.Post;
import com.inkwell.post.entity.PostStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Post entity with custom queries for atomic operations and searching
 */
@Repository
public interface PostRepository extends JpaRepository<Post, Integer> {

    /**
     * Find a post by its unique slug
     *
     * @param slug the post slug
     * @return Optional containing the post if found
     */
    Optional<Post> findBySlug(String slug);

    /**
     * Find all posts by status
     *
     * @param status the post status
     * @return list of posts with the given status
     */
    List<Post> findByStatus(PostStatus status);

    /**
     * Find all posts by author ID
     *
     * @param authorId the author ID
     * @return list of posts by the author
     */
    List<Post> findByAuthorId(Integer authorId);

    /**
     * Find all posts by author ID and status
     *
     * @param authorId the author ID
     * @param status the post status
     * @return list of posts by the author with given status
     */
    List<Post> findByAuthorIdAndStatus(Integer authorId, PostStatus status);

    /**
     * Search posts by keyword in title or content
     * Uses LIKE operator for flexible text search
     *
     * @param keyword the search keyword
     * @return list of matching posts
     */
    @Query("SELECT p FROM Post p WHERE LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "ORDER BY p.createdAt DESC")
    List<Post> searchByKeyword(@Param("keyword") String keyword);

    /**
     * Atomically increment view count for a post
     * Uses UPDATE query to avoid race conditions during concurrent hits
     *
     * @param postId the post ID
     */
    @Modifying
    @Transactional
    @Query("UPDATE Post p SET p.viewCount = p.viewCount + 1 WHERE p.postId = :postId")
    void incrementViewCount(@Param("postId") Integer postId);

    /**
     * Atomically increment likes count for a post
     * Uses UPDATE query to avoid race conditions during concurrent hits
     *
     * @param postId the post ID
     */
    @Modifying
    @Transactional
    @Query("UPDATE Post p SET p.likesCount = p.likesCount + 1 WHERE p.postId = :postId")
    void incrementLikesCount(@Param("postId") Integer postId);

    /**
     * Atomically decrement likes count for a post
     *
     * @param postId the post ID
     */
    @Modifying
    @Transactional
    @Query("UPDATE Post p SET p.likesCount = CASE WHEN p.likesCount > 0 THEN p.likesCount - 1 ELSE 0 END WHERE p.postId = :postId")
    void decrementLikesCount(@Param("postId") Integer postId);

    /**
     * Check if a slug already exists
     *
     * @param slug the slug to check
     * @return true if slug exists, false otherwise
     */
    boolean existsBySlug(String slug);

    /**
     * Find published posts only
     *
     * @return list of published posts
     */
    @Query("SELECT p FROM Post p WHERE p.status = 'PUBLISHED' ORDER BY p.isFeatured DESC, p.publishedAt DESC")
    List<Post> findAllPublishedPosts();

    @Query("SELECT p FROM Post p ORDER BY p.viewCount DESC")
    List<Post> findTopMostViewedPosts(org.springframework.data.domain.Pageable pageable);

    @Query("SELECT p.authorId, COUNT(p) as postCount FROM Post p GROUP BY p.authorId ORDER BY postCount DESC")
    List<Object[]> findMostActiveAuthors(org.springframework.data.domain.Pageable pageable);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM post_tags WHERE post_id = :postId", nativeQuery = true)
    void deleteTagLinksByPostId(@Param("postId") Integer postId);
}
