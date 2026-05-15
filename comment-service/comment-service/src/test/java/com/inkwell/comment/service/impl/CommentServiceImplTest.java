package com.inkwell.comment.service.impl;

import com.inkwell.comment.dto.CommentDTO;
import com.inkwell.comment.dto.CommentRequest;
import com.inkwell.comment.client.AuthClient;
import com.inkwell.comment.client.MessagingClient;
import com.inkwell.comment.client.PostClient;
import com.inkwell.comment.entity.Comment;
import com.inkwell.comment.entity.CommentLike;
import com.inkwell.comment.entity.CommentSettings;
import com.inkwell.comment.entity.CommentStatus;
import com.inkwell.comment.exception.CommentNotFoundException;
import com.inkwell.comment.mapper.CommentMapper;
import com.inkwell.comment.repository.CommentLikeRepository;
import com.inkwell.comment.repository.CommentSettingsRepository;
import com.inkwell.comment.repository.CommentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceImplTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private CommentMapper commentMapper;

    @Mock
    private CommentSettingsRepository settingsRepository;

    @Mock
    private CommentLikeRepository commentLikeRepository;

    @Mock
    private PostClient postClient;

    @Mock
    private MessagingClient messagingClient;

    @Mock
    private AuthClient authClient;

    @InjectMocks
    private CommentServiceImpl commentService;

    private Comment testComment;
    private CommentDTO testCommentDTO;
    private CommentRequest createCommentRequest;
    private CommentRequest replyCommentRequest;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();

        testComment = Comment.builder()
                .commentId(1)
                .postId(1)
                .authorId(1)
                .parentCommentId(null)
                .content("This is a test comment")
                .likesCount(5)
                .status(CommentStatus.APPROVED)
                .createdAt(now)
                .updatedAt(now)
                .build();

        testCommentDTO = CommentDTO.builder()
                .commentId(1)
                .postId(1)
                .authorId(1)
                .parentCommentId(null)
                .content("This is a test comment")
                .likesCount(5)
                .status(CommentStatus.APPROVED)
                .createdAt(now)
                .updatedAt(now)
                .build();

        createCommentRequest = CommentRequest.builder()
                .postId(1)
                .authorId(1)
                .content("This is a new comment")
                .build();

        replyCommentRequest = CommentRequest.builder()
                .postId(1)
                .authorId(2)
                .parentCommentId(1)
                .content("This is a reply")
                .build();
    }

    @Test
    void testCreateComment_Success() {
        when(settingsRepository.findById(1)).thenReturn(Optional.empty());
        when(commentRepository.save(any(Comment.class))).thenReturn(testComment);
        when(commentMapper.toDTO(testComment)).thenReturn(testCommentDTO);
        when(postClient.getPostAuthorId(1)).thenReturn(2);

        CommentDTO result = commentService.createComment(createCommentRequest);

        assertNotNull(result);
        assertEquals(CommentStatus.APPROVED, result.getStatus());
        assertEquals("This is a test comment", result.getContent());
        verify(commentRepository).save(any(Comment.class));
        verify(commentMapper).toDTO(testComment);
        verify(messagingClient).sendNotification(2, 1, "NEW_COMMENT", "New Comment on your post",
                "Someone left a comment on your story.");
    }

    @Test
    void testCreateReply_Success() {
        Comment parentComment = Comment.builder()
                .commentId(1)
                .postId(1)
                .authorId(1)
                .parentCommentId(null)
                .status(CommentStatus.APPROVED)
                .build();

        Comment replyComment = Comment.builder()
                .commentId(2)
                .postId(1)
                .authorId(2)
                .parentCommentId(1)
                .content("This is a reply @mira")
                .status(CommentStatus.APPROVED)
                .build();

        when(commentRepository.findById(1)).thenReturn(Optional.of(parentComment));
        when(settingsRepository.findById(1)).thenReturn(Optional.empty());
        when(commentRepository.save(any(Comment.class))).thenReturn(replyComment);
        when(commentMapper.toDTO(replyComment)).thenReturn(
            CommentDTO.builder()
                .commentId(2)
                .postId(1)
                .authorId(2)
                .parentCommentId(1)
                .content("This is a reply")
                .status(CommentStatus.APPROVED)
                .build()
        );
        when(postClient.getPostAuthorId(1)).thenReturn(3);
        when(authClient.getUserIdByUsername("mira")).thenReturn(4);

        CommentDTO result = commentService.createComment(replyCommentRequest);

        assertNotNull(result);
        assertEquals(Integer.valueOf(1), result.getParentCommentId());
        verify(commentRepository, times(2)).findById(1);
        verify(commentRepository).save(any(Comment.class));
        verify(messagingClient).sendNotification(3, 2, "NEW_COMMENT", "New Comment on your post",
                "Someone left a comment on your story.");
        verify(messagingClient).sendNotification(1, 2, "COMMENT_REPLY", "New Reply to your comment",
                "Someone replied to your comment.");
        verify(messagingClient).sendNotification(4, 2, "COMMENT_MENTION", "You were mentioned",
                "Someone mentioned you in a comment.");
    }

    @Test
    void testCreateReply_ParentNotFound() {
        when(commentRepository.findById(1)).thenReturn(Optional.empty());

        CommentNotFoundException exception = assertThrows(CommentNotFoundException.class,
            () -> commentService.createComment(replyCommentRequest));

        assertEquals("Parent comment not found with id: 1", exception.getMessage());
    }

    @Test
    void testCreateReply_ReplyToReplyNotAllowed() {
        Comment parentComment = Comment.builder()
                .commentId(1)
                .postId(1)
                .authorId(1)
                .parentCommentId(5) // This is already a reply
                .status(CommentStatus.APPROVED)
                .build();

        when(commentRepository.findById(1)).thenReturn(Optional.of(parentComment));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> commentService.createComment(replyCommentRequest));

        assertEquals("Cannot reply to a reply. Threading is limited to 2 levels.", exception.getMessage());
    }

    @Test
    void testCreateReply_ParentNotApproved() {
        Comment parentComment = Comment.builder()
                .commentId(1)
                .postId(1)
                .authorId(1)
                .parentCommentId(null)
                .status(CommentStatus.PENDING)
                .build();

        when(commentRepository.findById(1)).thenReturn(Optional.of(parentComment));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> commentService.createComment(replyCommentRequest));

        assertEquals("Cannot reply to a comment that is not approved.", exception.getMessage());
    }

    @Test
    void testGetCommentById_Success() {
        when(commentRepository.findById(1)).thenReturn(Optional.of(testComment));
        when(commentMapper.toDTO(testComment)).thenReturn(testCommentDTO);

        CommentDTO result = commentService.getCommentById(1);

        assertNotNull(result);
        assertEquals(1, result.getCommentId());
        verify(commentRepository).findById(1);
        verify(commentMapper).toDTO(testComment);
    }

    @Test
    void testGetCommentById_NotFound() {
        when(commentRepository.findById(1)).thenReturn(Optional.empty());

        CommentNotFoundException exception = assertThrows(CommentNotFoundException.class,
            () -> commentService.getCommentById(1));

        assertEquals("Comment not found with id: 1", exception.getMessage());
    }

    @Test
    void testGetCommentsByPostId() {
        List<Comment> comments = Arrays.asList(testComment);
        when(commentRepository.findByPostIdAndStatus(1, CommentStatus.APPROVED)).thenReturn(comments);
        when(commentMapper.toDTO(testComment)).thenReturn(testCommentDTO);

        List<CommentDTO> result = commentService.getCommentsByPostId(1);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getCommentId());
        verify(commentRepository).findByPostIdAndStatus(1, CommentStatus.APPROVED);
    }

    @Test
    void testGetTopLevelCommentsByPostId() {
        List<Comment> comments = Arrays.asList(testComment);
        when(commentRepository.findTopLevelByPostIdAndStatus(1, CommentStatus.APPROVED)).thenReturn(comments);
        when(commentMapper.toDTO(testComment)).thenReturn(testCommentDTO);

        List<CommentDTO> result = commentService.getTopLevelCommentsByPostId(1);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(commentRepository).findTopLevelByPostIdAndStatus(1, CommentStatus.APPROVED);
    }

    @Test
    void testSoftDeleteComment_Success() {
        when(commentRepository.findById(1)).thenReturn(Optional.of(testComment));

        commentService.softDeleteComment(1);

        verify(commentRepository).softDelete(1, CommentStatus.DELETED, "This comment has been removed");
        verify(commentRepository).softDeleteReplies(1, CommentStatus.DELETED, "This thought was removed as the parent discussion was retracted.");
    }

    @Test
    void testSoftDeleteComment_NotFound() {
        when(commentRepository.findById(1)).thenReturn(Optional.empty());

        CommentNotFoundException exception = assertThrows(CommentNotFoundException.class,
            () -> commentService.softDeleteComment(1));

        assertEquals("Comment not found with id: 1", exception.getMessage());
    }

    @Test
    void testModerateDeleteComment_Success() {
        Comment olderComment = Comment.builder()
                .commentId(1)
                .postId(1)
                .authorId(1)
                .content("Needs removal")
                .status(CommentStatus.APPROVED)
                .createdAt(LocalDateTime.now().minusHours(2))
                .updatedAt(LocalDateTime.now().minusHours(2))
                .build();

        when(commentRepository.findById(1)).thenReturn(Optional.of(olderComment));

        commentService.moderateDeleteComment(1);

        verify(commentRepository).softDelete(1, CommentStatus.DELETED, "This comment has been removed by moderation");
        verify(commentRepository).softDeleteReplies(1, CommentStatus.DELETED, "This reply was removed because the parent discussion was moderated.");
    }

    @Test
    void testSoftDeleteComment_OutsideWindowThrowsIllegalState() {
        Comment olderComment = Comment.builder()
                .commentId(1)
                .postId(1)
                .authorId(1)
                .content("Too old")
                .status(CommentStatus.APPROVED)
                .createdAt(LocalDateTime.now().minusMinutes(16))
                .updatedAt(LocalDateTime.now().minusMinutes(16))
                .build();

        when(commentRepository.findById(1)).thenReturn(Optional.of(olderComment));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> commentService.softDeleteComment(1));

        assertEquals("Comments can only be deleteed within 15 minutes of posting.", exception.getMessage());
    }

    @Test
    void testLikeComment_Success() {
        when(commentRepository.existsById(1)).thenReturn(true);
        when(commentLikeRepository.existsByCommentIdAndUserId(1, 2)).thenReturn(false);

        boolean liked = commentService.likeComment(1, 2);

        assertTrue(liked);
        verify(commentLikeRepository).save(any(CommentLike.class));
        verify(commentRepository).incrementLikesCount(1);
    }

    @Test
    void testLikeComment_AlreadyLikedDoesNotIncrement() {
        when(commentRepository.existsById(1)).thenReturn(true);
        when(commentLikeRepository.existsByCommentIdAndUserId(1, 2)).thenReturn(true);

        boolean liked = commentService.likeComment(1, 2);

        assertFalse(liked);
        verify(commentRepository, never()).incrementLikesCount(1);
    }

    @Test
    void testLikeComment_NotFound() {
        when(commentRepository.existsById(1)).thenReturn(false);

        CommentNotFoundException exception = assertThrows(CommentNotFoundException.class,
            () -> commentService.likeComment(1, 2));

        assertEquals("Comment not found with id: 1", exception.getMessage());
    }

    @Test
    void testUnlikeComment_Success() {
        CommentLike like = CommentLike.builder().id(1L).commentId(1).userId(2).build();
        when(commentRepository.existsById(1)).thenReturn(true);
        when(commentLikeRepository.findByCommentIdAndUserId(1, 2)).thenReturn(Optional.of(like));

        boolean unliked = commentService.unlikeComment(1, 2);

        assertTrue(unliked);
        verify(commentLikeRepository).delete(like);
        verify(commentRepository).decrementLikesCount(1);
    }

    @Test
    void testGetCommentsByAuthorId() {
        when(commentRepository.findByAuthorId(1)).thenReturn(List.of(testComment));
        when(commentMapper.toDTO(testComment)).thenReturn(testCommentDTO);

        assertEquals(1, commentService.getCommentsByAuthorId(1).size());
    }

    @Test
    void testGetCommentCountByPostId() {
        when(commentRepository.countByPostIdAndStatus(1, CommentStatus.APPROVED)).thenReturn(3);

        assertEquals(3, commentService.getCommentCountByPostId(1));
    }

    @Test
    void testGetRepliesByParentCommentId() {
        when(commentRepository.findRepliesByParentCommentIdAndStatus(1, CommentStatus.APPROVED)).thenReturn(List.of(testComment));
        when(commentMapper.toDTO(testComment)).thenReturn(testCommentDTO);

        assertEquals(1, commentService.getRepliesByParentCommentId(1).size());
    }

    @Test
    void testUpdateComment_Success() {
        CommentRequest request = CommentRequest.builder().content("Updated").build();
        when(commentRepository.findById(1)).thenReturn(Optional.of(testComment));
        when(commentRepository.save(testComment)).thenReturn(testComment);
        when(commentMapper.toDTO(testComment)).thenReturn(testCommentDTO);

        CommentDTO result = commentService.updateComment(1, request);

        assertNotNull(result);
        assertEquals("Updated", testComment.getContent());
    }

    @Test
    void testApproveAndRejectComment() {
        when(commentRepository.findById(1)).thenReturn(Optional.of(testComment));

        commentService.approveComment(1);
        assertEquals(CommentStatus.APPROVED, testComment.getStatus());

        commentService.rejectComment(1);
        assertEquals(CommentStatus.REJECTED, testComment.getStatus());
        verify(commentRepository, times(2)).save(testComment);
    }

    @Test
    void testDeleteCommentsByPostId() {
        when(commentRepository.findByPostId(1)).thenReturn(List.of(testComment));

        commentService.deleteCommentsByPostId(1);

        verify(commentRepository).deleteAll(List.of(testComment));
    }

    @Test
    void testModerationModeUsesSettings() {
        CommentSettings settings = CommentSettings.builder().id(1).moderationRequired(true).build();
        when(settingsRepository.findById(1)).thenReturn(Optional.of(settings));

        assertTrue(commentService.getModerationMode());
    }

    @Test
    void testToggleModerationModeCreatesSettingsWhenMissing() {
        when(settingsRepository.findById(1)).thenReturn(Optional.empty());

        commentService.toggleModerationMode(true);

        verify(settingsRepository).save(any(CommentSettings.class));
    }
}
