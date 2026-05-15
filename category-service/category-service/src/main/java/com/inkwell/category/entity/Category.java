package com.inkwell.category.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Category Entity - Represents a hierarchical category structure.
 * Supports parent-child relationships (e.g., "Programming" -> "Java").
 */
@Entity
@Table(name = "categories", indexes = {
    @Index(name = "idx_slug", columnList = "slug", unique = true),
    @Index(name = "idx_parent_id", columnList = "parent_category_id"),
    @Index(name = "idx_created_at", columnList = "created_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer categoryId;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Foreign key to parent category for hierarchical structure.
     * Nullable to allow top-level categories.
     */
    @Column(name = "parent_category_id")
    private Integer parentCategoryId;

    /**
     * Denormalized count of posts in this category.
     * Incremented/decremented when posts are assigned/removed.
     */
    @Builder.Default
    private Integer postCount = 0;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Lifecycle hook to set creation timestamp.
     */
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Lifecycle hook to update modification timestamp.
     */
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

