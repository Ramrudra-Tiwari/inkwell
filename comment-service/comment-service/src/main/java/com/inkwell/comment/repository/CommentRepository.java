package com.inkwell.comment.repository;

import com.inkwell.comment.entity.Comment;
import com.inkwell.comment.entity.CommentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Integer> {

    List<Comment> findByPostId(Integer postId);

    List<Comment> findByPostIdAndStatusIn(Integer postId, List<CommentStatus> statuses);

    List<Comment> findByAuthorId(Integer authorId);

    List<Comment> findByPostIdAndStatus(Integer postId, CommentStatus status);

    Integer countByPostIdAndStatus(Integer postId, CommentStatus status);

    @Query("SELECT c FROM Comment c WHERE c.postId = :postId AND c.parentCommentId IS NULL")
    List<Comment> findTopLevelByPostId(@Param("postId") Integer postId);

    @Query("SELECT c FROM Comment c WHERE c.postId = :postId AND c.parentCommentId IS NULL AND c.status = :status")
    List<Comment> findTopLevelByPostIdAndStatus(@Param("postId") Integer postId, @Param("status") CommentStatus status);

    @Query("SELECT c FROM Comment c WHERE c.parentCommentId = :parentCommentId")
    List<Comment> findRepliesByParentCommentId(@Param("parentCommentId") Integer parentCommentId);

    @Query("SELECT c FROM Comment c WHERE c.parentCommentId = :parentCommentId AND c.status = :status")
    List<Comment> findRepliesByParentCommentIdAndStatus(@Param("parentCommentId") Integer parentCommentId, @Param("status") CommentStatus status);

    @Modifying
    @Transactional
    @Query("UPDATE Comment c SET c.likesCount = c.likesCount + 1 WHERE c.commentId = :commentId")
    void incrementLikesCount(@Param("commentId") Integer commentId);

    @Modifying
    @Transactional
    @Query("UPDATE Comment c SET c.likesCount = c.likesCount - 1 WHERE c.commentId = :commentId AND c.likesCount > 0")
    void decrementLikesCount(@Param("commentId") Integer commentId);

    @Modifying
    @Transactional
    @Query("UPDATE Comment c SET c.status = :status, c.content = :content WHERE c.commentId = :commentId")
    void softDelete(@Param("commentId") Integer commentId, @Param("status") CommentStatus status, @Param("content") String content);

    @Modifying
    @Transactional
    @Query("UPDATE Comment c SET c.status = :status, c.content = :content WHERE c.parentCommentId = :parentCommentId")
    void softDeleteReplies(@Param("parentCommentId") Integer parentCommentId, @Param("status") CommentStatus status, @Param("content") String content);
}
