package com.inkwell.comment.controller;

import com.inkwell.comment.dto.CommentDTO;
import com.inkwell.comment.dto.CommentRequest;
import com.inkwell.comment.service.CommentService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CommentResourceTest {

    @Test
    void createAndReadEndpointsReturnServiceValues() {
        CommentService commentService = mock(CommentService.class);
        CommentResource resource = new CommentResource(commentService);
        CommentRequest request = CommentRequest.builder().postId(1).authorId(2).content("Nice").build();
        CommentDTO dto = CommentDTO.builder().commentId(1).content("Nice").build();
        when(commentService.createComment(request)).thenReturn(dto);
        when(commentService.getCommentById(1)).thenReturn(dto);
        when(commentService.getCommentsByPostId(1)).thenReturn(List.of(dto));
        when(commentService.getCommentsByAuthorId(2)).thenReturn(List.of(dto));
        when(commentService.getTopLevelCommentsByPostId(1)).thenReturn(List.of(dto));
        when(commentService.getRepliesByParentCommentId(1)).thenReturn(List.of(dto));
        when(commentService.getCommentCountByPostId(1)).thenReturn(5);

        assertEquals(HttpStatus.CREATED, resource.createComment(request).getStatusCode());
        assertEquals(dto, resource.getCommentById(1).getBody());
        assertEquals(1, resource.getCommentsByPostId(1).getBody().size());
        assertEquals(1, resource.getCommentsByAuthorId(2).getBody().size());
        assertEquals(1, resource.getTopLevelCommentsByPostId(1).getBody().size());
        assertEquals(1, resource.getRepliesByParentCommentId(1).getBody().size());
        assertEquals(5, resource.getCommentCountByPostId(1).getBody());
    }

    @Test
    void mutationEndpointsDelegateToService() {
        CommentService commentService = mock(CommentService.class);
        CommentResource resource = new CommentResource(commentService);
        CommentRequest request = CommentRequest.builder().content("Updated").build();
        CommentDTO dto = CommentDTO.builder().commentId(1).content("Updated").build();
        when(commentService.updateComment(1, request)).thenReturn(dto);
        when(commentService.getModerationMode()).thenReturn(true);

        assertEquals(dto, resource.updateComment(1, request).getBody());
        assertEquals(HttpStatus.NO_CONTENT, resource.deleteComment(1).getStatusCode());
        assertEquals(HttpStatus.NO_CONTENT, resource.moderateDeleteComment(1).getStatusCode());
        assertEquals(HttpStatus.NO_CONTENT, resource.deleteCommentsByPostId(1).getStatusCode());
        assertEquals(HttpStatus.NO_CONTENT, resource.approveComment(1).getStatusCode());
        assertEquals(HttpStatus.NO_CONTENT, resource.rejectComment(1).getStatusCode());
        when(commentService.likeComment(1, 2)).thenReturn(true);

        assertEquals(HttpStatus.CREATED, resource.likeComment(1, 2).getStatusCode());
        assertEquals(HttpStatus.NO_CONTENT, resource.unlikeComment(1, 2).getStatusCode());
        assertEquals(HttpStatus.UNAUTHORIZED, resource.likeComment(1, null).getStatusCode());
        assertEquals(HttpStatus.NO_CONTENT, resource.toggleModeration(true).getStatusCode());
        assertEquals(true, resource.getModerationMode().getBody());

        verify(commentService).softDeleteComment(1);
        verify(commentService).moderateDeleteComment(1);
        verify(commentService).deleteCommentsByPostId(1);
        verify(commentService).approveComment(1);
        verify(commentService).rejectComment(1);
        verify(commentService).likeComment(1, 2);
        verify(commentService).unlikeComment(1, 2);
        verify(commentService).toggleModerationMode(true);
    }
}
