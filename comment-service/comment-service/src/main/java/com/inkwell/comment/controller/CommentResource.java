package com.inkwell.comment.controller;

import com.inkwell.comment.dto.CommentDTO;
import com.inkwell.comment.dto.CommentRequest;
import com.inkwell.comment.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/comments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Comment Service", description = "API endpoints for managing blog comments")
public class CommentResource {

    private final CommentService commentService;

    @PostMapping
    @Operation(summary = "Create a new comment", description = "Creates a new comment or reply. Comments default to APPROVED status.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Comment created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request body or threading violation"),
        @ApiResponse(responseCode = "404", description = "Parent comment not found (for replies)")
    })
    public ResponseEntity<CommentDTO> createComment(@Valid @RequestBody CommentRequest request) {
        log.info("Received request to create comment for postId: {}", request.getPostId());
        CommentDTO comment = commentService.createComment(request);
        return new ResponseEntity<>(comment, HttpStatus.CREATED);
    }

    @GetMapping("/{commentId}")
    @Operation(summary = "Get comment by ID", description = "Retrieves a specific comment by its ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Comment found"),
        @ApiResponse(responseCode = "404", description = "Comment not found")
    })
    public ResponseEntity<CommentDTO> getCommentById(
            @Parameter(description = "Comment ID") @PathVariable Integer commentId) {
        log.debug("Received request to get comment by id: {}", commentId);
        CommentDTO comment = commentService.getCommentById(commentId);
        return ResponseEntity.ok(comment);
    }

    @GetMapping("/post/{postId}")
    @Operation(summary = "Get all comments for a post", description = "Retrieves all approved comments for a specific post")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Comments retrieved successfully")
    })
    public ResponseEntity<List<CommentDTO>> getCommentsByPostId(
            @Parameter(description = "Post ID") @PathVariable Integer postId) {
        log.debug("Received request to get comments for postId: {}", postId);
        List<CommentDTO> comments = commentService.getCommentsByPostId(postId);
        return ResponseEntity.ok(comments);
    }

    @GetMapping("/post/{postId}/moderation")
    @Operation(summary = "Get moderation comments for a post", description = "Retrieves comments for moderation including pending and rejected items for a specific post")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Moderation comments retrieved successfully")
    })
    public ResponseEntity<List<CommentDTO>> getCommentsByPostIdForModeration(
            @Parameter(description = "Post ID") @PathVariable Integer postId) {
        log.debug("Received request to get moderation comments for postId: {}", postId);
        List<CommentDTO> comments = commentService.getCommentsByPostIdForModeration(postId);
        return ResponseEntity.ok(comments);
    }

    @GetMapping("/author/{authorId}")
    @Operation(summary = "Get all comments by an author", description = "Retrieves all comments written by a specific author")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Comments retrieved successfully")
    })
    public ResponseEntity<List<CommentDTO>> getCommentsByAuthorId(
            @Parameter(description = "Author ID") @PathVariable Integer authorId) {
        log.debug("Received request to get comments for authorId: {}", authorId);
        List<CommentDTO> comments = commentService.getCommentsByAuthorId(authorId);
        return ResponseEntity.ok(comments);
    }

    @GetMapping("/post/{postId}/count")
    @Operation(summary = "Get comment count for a post", description = "Returns the total number of approved comments for a specific post")
    public ResponseEntity<Integer> getCommentCountByPostId(@PathVariable Integer postId) {
        log.debug("Received request to get comment count for postId: {}", postId);
        return ResponseEntity.ok(commentService.getCommentCountByPostId(postId));
    }

    @GetMapping("/post/{postId}/top-level")
    @Operation(summary = "Get top-level comments for a post", description = "Retrieves only top-level comments (not replies) for a specific post")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Top-level comments retrieved successfully")
    })
    public ResponseEntity<List<CommentDTO>> getTopLevelCommentsByPostId(
            @Parameter(description = "Post ID") @PathVariable Integer postId) {
        log.debug("Received request to get top-level comments for postId: {}", postId);
        List<CommentDTO> comments = commentService.getTopLevelCommentsByPostId(postId);
        return ResponseEntity.ok(comments);
    }

    @GetMapping("/{parentCommentId}/replies")
    @Operation(summary = "Get replies for a comment", description = "Retrieves all approved replies for a specific parent comment")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Replies retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Parent comment not found")
    })
    public ResponseEntity<List<CommentDTO>> getRepliesByParentCommentId(
            @Parameter(description = "Parent Comment ID") @PathVariable Integer parentCommentId) {
        log.debug("Received request to get replies for parent comment id: {}", parentCommentId);
        List<CommentDTO> replies = commentService.getRepliesByParentCommentId(parentCommentId);
        return ResponseEntity.ok(replies);
    }

    @PutMapping("/{commentId}")
    @Operation(summary = "Update a comment", description = "Updates the content of an existing comment")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Comment updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request body"),
        @ApiResponse(responseCode = "404", description = "Comment not found")
    })
    public ResponseEntity<CommentDTO> updateComment(
            @Parameter(description = "Comment ID") @PathVariable Integer commentId,
            @Valid @RequestBody CommentRequest request) {
        log.info("Received request to update comment id: {}", commentId);
        CommentDTO comment = commentService.updateComment(commentId, request);
        return ResponseEntity.ok(comment);
    }

    @DeleteMapping("/{commentId}")
    @Operation(summary = "Soft delete a comment", description = "Soft deletes a comment by setting status to DELETED and clearing content")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Comment soft deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Comment not found")
    })
    public ResponseEntity<Void> deleteComment(
            @Parameter(description = "Comment ID") @PathVariable Integer commentId) {
        log.info("Received request to delete comment id: {}", commentId);
        commentService.softDeleteComment(commentId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{commentId}/moderate-delete")
    @Operation(summary = "Moderation delete a comment", description = "Soft deletes a comment for moderation without the author edit window restriction")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Comment removed by moderation successfully"),
        @ApiResponse(responseCode = "404", description = "Comment not found")
    })
    public ResponseEntity<Void> moderateDeleteComment(
            @Parameter(description = "Comment ID") @PathVariable Integer commentId) {
        log.info("Received request to moderation-delete comment id: {}", commentId);
        commentService.moderateDeleteComment(commentId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/post/{postId}")
    @Operation(summary = "Delete all comments for a post", description = "Permanently removes all comments associated with a specific post (used when post is deleted)")
    public ResponseEntity<Void> deleteCommentsByPostId(@PathVariable Integer postId) {
        log.info("Received request to delete all comments for postId: {}", postId);
        commentService.deleteCommentsByPostId(postId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{commentId}/approve")
    @Operation(summary = "Approve a comment", description = "Changes comment status to APPROVED (for moderation)")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Comment approved successfully"),
        @ApiResponse(responseCode = "404", description = "Comment not found")
    })
    public ResponseEntity<Void> approveComment(
            @Parameter(description = "Comment ID") @PathVariable Integer commentId) {
        log.info("Received request to approve comment id: {}", commentId);
        commentService.approveComment(commentId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{commentId}/reject")
    @Operation(summary = "Reject a comment", description = "Changes comment status to REJECTED (for moderation)")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Comment rejected successfully"),
        @ApiResponse(responseCode = "404", description = "Comment not found")
    })
    public ResponseEntity<Void> rejectComment(
            @Parameter(description = "Comment ID") @PathVariable Integer commentId) {
        log.info("Received request to reject comment id: {}", commentId);
        commentService.rejectComment(commentId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{commentId}/like")
    @Operation(summary = "Like a comment", description = "Increments the likes count for a comment")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Comment liked successfully"),
        @ApiResponse(responseCode = "404", description = "Comment not found")
    })
    public ResponseEntity<Void> likeComment(
            @Parameter(description = "Comment ID") @PathVariable Integer commentId,
            @RequestHeader(value = "X-User-Id", required = false) Integer userId) {
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        log.debug("Received request to like comment id: {} for user id: {}", commentId, userId);
        boolean created = commentService.likeComment(commentId, userId);
        return created
                ? ResponseEntity.status(HttpStatus.CREATED).build()
                : ResponseEntity.noContent().build();
    }

    @PostMapping("/{commentId}/unlike")
    @Operation(summary = "Unlike a comment", description = "Decrements the likes count for a comment")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Comment unliked successfully"),
        @ApiResponse(responseCode = "404", description = "Comment not found")
    })
    public ResponseEntity<Void> unlikeComment(
            @Parameter(description = "Comment ID") @PathVariable Integer commentId,
            @RequestHeader(value = "X-User-Id", required = false) Integer userId) {
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        log.debug("Received request to unlike comment id: {} for user id: {}", commentId, userId);
        commentService.unlikeComment(commentId, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/settings/moderation")
    @Operation(summary = "Toggle moderation mode", description = "Enables or disables platform-wide comment moderation requirement")
    public ResponseEntity<Void> toggleModeration(@RequestParam boolean required) {
        log.info("Admin request to toggle moderation mode to: {}", required);
        commentService.toggleModerationMode(required);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/settings/moderation")
    @Operation(summary = "Get moderation mode", description = "Checks if platform-wide comment moderation is required")
    public ResponseEntity<Boolean> getModerationMode() {
        return ResponseEntity.ok(commentService.getModerationMode());
    }
}
