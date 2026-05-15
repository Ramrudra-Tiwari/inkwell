package com.inkwell.post.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Post Entity - Represents a blog post in the system
 *
 * A post can be created in DRAFT status and later published.
 * Read time is calculated based on word count (200 WPM).
 * Slug is unique and automatically generated from the title.
 */
@Entity
@Table(name = "posts", indexes = {
    @Index(name = "idx_slug", columnList = "slug", unique = true),
    @Index(name = "idx_author_id", columnList = "author_id"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_created_at", columnList = "created_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer postId;

    @Column(nullable = false)
    private Integer authorId;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(unique = true, nullable = false, length = 255)
    private String slug;

    @Column(columnDefinition = "LONGTEXT", nullable = false)
    private String content; // Rich HTML content

    @Column(length = 500)
    private String excerpt;

    private String featuredImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PostStatus status = PostStatus.DRAFT;

    @Column(nullable = false)
    @Builder.Default
    private Integer readTimeMin = 1; // Minimum 1 minute

    @Column(nullable = false)
    @Builder.Default
    private Integer viewCount = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer likesCount = 0;

    @Column(nullable = false)
    @Builder.Default
    private boolean isFeatured = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    private LocalDateTime publishedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "post_tags",
        joinColumns = @JoinColumn(name = "post_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    @Builder.Default
    private java.util.Set<Tag> tags = new java.util.HashSet<>();

    /**
     * JPA lifecycle callback - Sets createdAt and updatedAt before persisting
     */
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * JPA lifecycle callback - Updates updatedAt before updating
     */
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

