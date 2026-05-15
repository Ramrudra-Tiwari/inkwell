package com.inkwell.comment.service;

import com.inkwell.comment.dto.CommentDTO;
import com.inkwell.comment.dto.CommentRequest;

import java.util.List;

public interface CommentService {

    CommentDTO createComment(CommentRequest request);

    CommentDTO getCommentById(Integer commentId);

    List<CommentDTO> getCommentsByPostId(Integer postId);

    List<CommentDTO> getCommentsByPostIdForModeration(Integer postId);

    List<CommentDTO> getCommentsByAuthorId(Integer authorId);

    Integer getCommentCountByPostId(Integer postId);

    List<CommentDTO> getTopLevelCommentsByPostId(Integer postId);

    List<CommentDTO> getRepliesByParentCommentId(Integer parentCommentId);

    CommentDTO updateComment(Integer commentId, CommentRequest request);

    void softDeleteComment(Integer commentId);

    void moderateDeleteComment(Integer commentId);

    void approveComment(Integer commentId);

    void rejectComment(Integer commentId);

    boolean likeComment(Integer commentId, Integer userId);

    boolean unlikeComment(Integer commentId, Integer userId);

    void deleteCommentsByPostId(Integer postId);

    void toggleModerationMode(boolean required);

    boolean getModerationMode();
}
