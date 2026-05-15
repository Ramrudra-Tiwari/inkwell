package com.inkwell.comment.repository;

import com.inkwell.comment.entity.CommentLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {

    boolean existsByCommentIdAndUserId(Integer commentId, Integer userId);

    Optional<CommentLike> findByCommentIdAndUserId(Integer commentId, Integer userId);
}
