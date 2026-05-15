package com.inkwell.category.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Tag Entity - Represents a flat tag structure for post tagging.
 * Tags help organize posts by topic without hierarchy.
 */
@Entity
@Table(name = "tags", indexes = {
    @Index(name = "idx_tag_slug", columnList = "slug", unique = true),
    @Index(name = "idx_tag_created_at", columnList = "created_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer tagId;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String slug;

    /**
     * Denormalized count of posts tagged with this tag.
     * Incremented/decremented when posts are tagged/untagged.
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

