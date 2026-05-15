package com.inkwell.comment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "comment_likes", uniqueConstraints = {
        @UniqueConstraint(name = "uk_comment_like_user", columnNames = {"comment_id", "user_id"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "comment_id", nullable = false)
    private Integer commentId;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "liked_at", nullable = false, updatable = false)
    private LocalDateTime likedAt;

    @PrePersist
    void prePersist() {
        likedAt = LocalDateTime.now();
    }
}
