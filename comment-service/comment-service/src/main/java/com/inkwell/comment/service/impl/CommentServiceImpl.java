package com.inkwell.comment.service.impl;

import com.inkwell.comment.dto.CommentDTO;
import com.inkwell.comment.dto.CommentRequest;
import com.inkwell.comment.entity.Comment;
import com.inkwell.comment.entity.CommentLike;
import com.inkwell.comment.entity.CommentStatus;
import com.inkwell.comment.exception.CommentNotFoundException;
import com.inkwell.comment.mapper.CommentMapper;
import com.inkwell.comment.repository.CommentLikeRepository;
import com.inkwell.comment.repository.CommentRepository;
import com.inkwell.comment.service.CommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;
    private final com.inkwell.comment.client.PostClient postClient;
    private final com.inkwell.comment.client.MessagingClient messagingClient;
    private final com.inkwell.comment.client.AuthClient authClient;
    private final com.inkwell.comment.repository.CommentSettingsRepository settingsRepository;
    private final CommentLikeRepository commentLikeRepository;

    private boolean isModerationRequired() {
        return settingsRepository.findById(1)
                .map(com.inkwell.comment.entity.CommentSettings::isModerationRequired)
                .orElse(false);
    }

    @Override
    public CommentDTO createComment(CommentRequest request) {
        log.info("Creating new comment for postId: {}, authorId: {}", request.getPostId(), request.getAuthorId());

        // Validate threading logic - limit to 2 levels
        if (request.getParentCommentId() != null) {
            validateParentComment(request.getParentCommentId());
        }

        Comment comment = Comment.builder()
                .postId(request.getPostId())
                .authorId(request.getAuthorId())
                .parentCommentId(request.getParentCommentId())
                .content(request.getContent())
                .status(isModerationRequired() ? CommentStatus.PENDING : CommentStatus.APPROVED)
                .build();

        Comment savedComment = commentRepository.save(comment);
        log.info("Comment created with id: {}", savedComment.getCommentId());

        // Requirement 2.3 & 2.7: Trigger Notifications
        triggerNotifications(savedComment);

        return commentMapper.toDTO(savedComment);
    }

    private void triggerNotifications(Comment comment) {
        try {
            // 1. Notify Post Author
            Integer postAuthorId = postClient.getPostAuthorId(comment.getPostId());
            if (postAuthorId != null && !postAuthorId.equals(comment.getAuthorId())) {
                messagingClient.sendNotification(
                    postAuthorId, 
                    comment.getAuthorId(), 
                    "NEW_COMMENT", 
                    "New Comment on your post", 
                    "Someone left a comment on your story."
                );
            }

            // 2. Notify Parent Comment Author (if reply)
            if (comment.getParentCommentId() != null) {
                commentRepository.findById(comment.getParentCommentId()).ifPresent(parent -> {
                    if (parent.getAuthorId() != null && !parent.getAuthorId().equals(comment.getAuthorId())) {
                        messagingClient.sendNotification(
                            parent.getAuthorId(),
                            comment.getAuthorId(),
                            "COMMENT_REPLY",
                            "New Reply to your comment",
                            "Someone replied to your comment."
                        );
                    }
                });
            }

            // 3. Notify @Mentions
            java.util.Set<String> mentions = parseMentions(comment.getContent());
            for (String username : mentions) {
                Integer mentionedUserId = authClient.getUserIdByUsername(username);
                if (mentionedUserId != null && !mentionedUserId.equals(comment.getAuthorId())) {
                    messagingClient.sendNotification(
                        mentionedUserId,
                        comment.getAuthorId(),
                        "COMMENT_MENTION",
                        "You were mentioned",
                        "Someone mentioned you in a comment."
                    );
                }
            }
        } catch (Exception e) {
            log.error("Failed to trigger notifications for comment: {}", comment.getCommentId(), e);
        }
    }

    private java.util.Set<String> parseMentions(String content) {
        java.util.Set<String> mentions = new java.util.HashSet<>();
        if (content == null) return mentions;
        
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("@(\\w+)").matcher(content);
        while (matcher.find()) {
            mentions.add(matcher.group(1));
        }
        return mentions;
    }

    @Override
    @Transactional(readOnly = true)
    public CommentDTO getCommentById(Integer commentId) {
        log.debug("Fetching comment by id: {}", commentId);
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException("Comment not found with id: " + commentId));
        return commentMapper.toDTO(comment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentDTO> getCommentsByPostId(Integer postId) {
        log.debug("Fetching all comments for postId: {}", postId);
        List<Comment> comments = commentRepository.findByPostIdAndStatus(postId, CommentStatus.APPROVED);
        return comments.stream()
                .map(commentMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentDTO> getCommentsByPostIdForModeration(Integer postId) {
        log.debug("Fetching moderation comments for postId: {}", postId);
        List<Comment> comments = commentRepository.findByPostIdAndStatusIn(
                postId,
                List.of(CommentStatus.PENDING, CommentStatus.REJECTED, CommentStatus.APPROVED)
        );
        return comments.stream()
                .map(commentMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentDTO> getCommentsByAuthorId(Integer authorId) {
        log.debug("Fetching all comments for authorId: {}", authorId);
        List<Comment> comments = commentRepository.findByAuthorId(authorId);
        return comments.stream()
                .map(commentMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Integer getCommentCountByPostId(Integer postId) {
        log.debug("Counting all approved comments for postId: {}", postId);
        return commentRepository.countByPostIdAndStatus(postId, CommentStatus.APPROVED);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentDTO> getTopLevelCommentsByPostId(Integer postId) {
        log.debug("Fetching top-level comments for postId: {}", postId);
        List<Comment> comments = commentRepository.findTopLevelByPostIdAndStatus(postId, CommentStatus.APPROVED);
        return comments.stream()
                .map(commentMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentDTO> getRepliesByParentCommentId(Integer parentCommentId) {
        log.debug("Fetching replies for parent comment id: {}", parentCommentId);
        List<Comment> replies = commentRepository.findRepliesByParentCommentIdAndStatus(parentCommentId, CommentStatus.APPROVED);
        return replies.stream()
                .map(commentMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public CommentDTO updateComment(Integer commentId, CommentRequest request) {
        log.info("Updating comment with id: {}", commentId);

        Comment existingComment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException("Comment not found with id: " + commentId));

        // Requirement 2.3: 15-minute window for edits
        validateTimeWindow(existingComment, "edit");

        // Validate threading logic if parentCommentId is being changed
        if (request.getParentCommentId() != null &&
            !request.getParentCommentId().equals(existingComment.getParentCommentId())) {
            validateParentComment(request.getParentCommentId());
        }

        existingComment.setContent(request.getContent());
        if (request.getParentCommentId() != null) {
            existingComment.setParentCommentId(request.getParentCommentId());
        }

        Comment updatedComment = commentRepository.save(existingComment);
        log.info("Comment updated with id: {}", updatedComment.getCommentId());

        return commentMapper.toDTO(updatedComment);
    }

    @Override
    public void softDeleteComment(Integer commentId) {
        log.info("Soft deleting comment with id: {}", commentId);

        Comment existingComment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException("Comment not found with id: " + commentId));

        // Requirement 2.3: 15-minute window for deletions
        validateTimeWindow(existingComment, "delete");

        commentRepository.softDelete(commentId, CommentStatus.DELETED, "This comment has been removed");
        
        // Requirement 2.7: Cascade soft delete to replies
        commentRepository.softDeleteReplies(commentId, CommentStatus.DELETED, "This thought was removed as the parent discussion was retracted.");
        
        log.info("Comment and its replies soft deleted with id: {}", commentId);
    }

    @Override
    public void moderateDeleteComment(Integer commentId) {
        log.info("Moderation delete requested for comment with id: {}", commentId);

        commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException("Comment not found with id: " + commentId));

        commentRepository.softDelete(commentId, CommentStatus.DELETED, "This comment has been removed by moderation");
        commentRepository.softDeleteReplies(commentId, CommentStatus.DELETED, "This reply was removed because the parent discussion was moderated.");

        log.info("Comment and its replies moderated out with id: {}", commentId);
    }

    @Override
    public void toggleModerationMode(boolean required) {
        log.info("Setting global comment moderation mode to: {}", required);
        com.inkwell.comment.entity.CommentSettings settings = settingsRepository.findById(1)
                .orElse(com.inkwell.comment.entity.CommentSettings.builder().id(1).build());
        settings.setModerationRequired(required);
        settingsRepository.save(settings);
    }

    @Override
    public boolean getModerationMode() {
        return isModerationRequired();
    }

    private void validateTimeWindow(Comment comment, String action) {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        java.time.Duration duration = java.time.Duration.between(comment.getCreatedAt(), now);
        if (duration.toMinutes() > 15) {
            log.warn("Comment {} attempt failed: {} minutes elapsed since creation", action, duration.toMinutes());
            throw new IllegalStateException("Comments can only be " + action + "ed within 15 minutes of posting.");
        }
    }

    @Override
    public void approveComment(Integer commentId) {
        log.info("Approving comment with id: {}", commentId);
        updateCommentStatus(commentId, CommentStatus.APPROVED);
    }

    @Override
    public void rejectComment(Integer commentId) {
        log.info("Rejecting comment with id: {}", commentId);
        updateCommentStatus(commentId, CommentStatus.REJECTED);
    }

    @Override
    public void deleteCommentsByPostId(Integer postId) {
        log.info("Permanently deleting all comments for post id: {}", postId);
        List<Comment> comments = commentRepository.findByPostId(postId);
        commentRepository.deleteAll(comments);
        log.info("Deleted {} comments for post id: {}", comments.size(), postId);
    }

    @Override
    public boolean likeComment(Integer commentId, Integer userId) {
        log.debug("Liking comment id: {} for user id: {}", commentId, userId);

        if (!commentRepository.existsById(commentId)) {
            throw new CommentNotFoundException("Comment not found with id: " + commentId);
        }

        if (commentLikeRepository.existsByCommentIdAndUserId(commentId, userId)) {
            return false;
        }

        commentLikeRepository.save(CommentLike.builder()
                .commentId(commentId)
                .userId(userId)
                .build());
        commentRepository.incrementLikesCount(commentId);
        return true;
    }

    @Override
    public boolean unlikeComment(Integer commentId, Integer userId) {
        log.debug("Unliking comment id: {} for user id: {}", commentId, userId);

        if (!commentRepository.existsById(commentId)) {
            throw new CommentNotFoundException("Comment not found with id: " + commentId);
        }

        return commentLikeRepository.findByCommentIdAndUserId(commentId, userId)
                .map(like -> {
                    commentLikeRepository.delete(like);
                    commentRepository.decrementLikesCount(commentId);
                    return true;
                })
                .orElse(false);
    }

    private void updateCommentStatus(Integer commentId, CommentStatus status) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException("Comment not found with id: " + commentId));

        comment.setStatus(status);
        commentRepository.save(comment);
        log.info("Comment {} status updated to {}", commentId, status);
    }

    private void validateParentComment(Integer parentCommentId) {
        Comment parentComment = commentRepository.findById(parentCommentId)
                .orElseThrow(() -> new CommentNotFoundException("Parent comment not found with id: " + parentCommentId));

        // Ensure parent comment is not itself a reply (limit to 2 levels)
        if (parentComment.getParentCommentId() != null) {
            throw new IllegalArgumentException("Cannot reply to a reply. Threading is limited to 2 levels.");
        }

        // Ensure parent comment is approved
        if (parentComment.getStatus() != CommentStatus.APPROVED) {
            throw new IllegalArgumentException("Cannot reply to a comment that is not approved.");
        }
    }
}
