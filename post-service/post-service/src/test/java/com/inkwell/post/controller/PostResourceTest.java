package com.inkwell.post.controller;

import com.inkwell.post.dto.CreatePostRequest;
import com.inkwell.post.dto.PostDTO;
import com.inkwell.post.dto.UpdatePostRequest;
import com.inkwell.post.entity.PostStatus;
import com.inkwell.post.service.PostService;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PostResourceTest {

    @Test
    void createPost_returnsCreatedPost() {
        PostService postService = mock(PostService.class);
        PostResource resource = new PostResource(postService);
        CreatePostRequest request = CreatePostRequest.builder().title("Story").authorId(1).content("Body").build();
        PostDTO dto = PostDTO.builder().postId(1).title("Story").build();
        when(postService.createPost(request)).thenReturn(dto);

        assertEquals(HttpStatus.CREATED, resource.createPost(request).getStatusCode());
    }

    @Test
    void readEndpointsReturnServiceValues() {
        PostService postService = mock(PostService.class);
        PostResource resource = new PostResource(postService);
        PostDTO dto = PostDTO.builder().postId(1).slug("story").build();
        when(postService.getAllPosts()).thenReturn(List.of(dto));
        when(postService.getPublishedPosts()).thenReturn(List.of(dto));
        when(postService.getPostBySlug("story")).thenReturn(dto);
        when(postService.getPostsByAuthor(7)).thenReturn(List.of(dto));
        when(postService.getPostsByAuthorAndStatus(7, PostStatus.PUBLISHED)).thenReturn(List.of(dto));
        when(postService.searchPosts("story")).thenReturn(List.of(dto));

        assertEquals(1, resource.getAllPosts().getBody().size());
        assertEquals(1, resource.getPublishedPosts().getBody().size());
        assertEquals(dto, resource.getPostBySlug("story").getBody());
        assertEquals(1, resource.getPostsByAuthor(7).getBody().size());
        assertEquals(1, resource.getPostsByAuthorAndStatus(7, PostStatus.PUBLISHED).getBody().size());
        assertEquals(1, resource.searchPosts("story").getBody().size());
    }

    @Test
    void getPostById_incrementsViewForSession() {
        PostService postService = mock(PostService.class);
        PostResource resource = new PostResource(postService);
        HttpSession session = mock(HttpSession.class);
        PostDTO dto = PostDTO.builder().postId(1).build();
        when(postService.getPostById(1)).thenReturn(dto);
        when(session.getId()).thenReturn("session-1");

        assertEquals(dto, resource.getPostById(1, session).getBody());
        verify(postService).incrementViewCount(1, "session-1");
    }

    @Test
    void mutationEndpointsDelegateToService() {
        PostService postService = mock(PostService.class);
        PostResource resource = new PostResource(postService);
        PostDTO dto = PostDTO.builder().postId(1).build();
        UpdatePostRequest request = UpdatePostRequest.builder().title("Updated").build();
        when(postService.updatePost(1, request)).thenReturn(dto);
        when(postService.publishPost(1)).thenReturn(dto);
        when(postService.unpublishPost(1)).thenReturn(dto);
        when(postService.featurePost(1, true)).thenReturn(dto);
        when(postService.likePost(1, 7)).thenReturn(true);

        assertEquals(dto, resource.updatePost(1, request).getBody());
        assertEquals(dto, resource.publishPost(1).getBody());
        assertEquals(dto, resource.unpublishPost(1).getBody());
        assertEquals(dto, resource.featurePost(1, true).getBody());
        assertEquals(HttpStatus.CREATED, resource.likePost(1, 7).getStatusCode());
        assertEquals(HttpStatus.NO_CONTENT, resource.unlikePost(1, 7).getStatusCode());
        assertEquals(HttpStatus.NO_CONTENT, resource.deletePost(1).getStatusCode());
        assertEquals(HttpStatus.NO_CONTENT, resource.deletePostsByAuthor(7).getStatusCode());
        verify(postService).deletePostsByAuthor(7);
    }

    @Test
    void likeEndpointReturnsNoContentWhenAlreadyLiked() {
        PostService postService = mock(PostService.class);
        PostResource resource = new PostResource(postService);
        when(postService.likePost(1, 7)).thenReturn(false);

        assertEquals(HttpStatus.NO_CONTENT, resource.likePost(1, 7).getStatusCode());
    }

    @Test
    void likeEndpointsRequireUserHeader() {
        PostService postService = mock(PostService.class);
        PostResource resource = new PostResource(postService);

        assertEquals(HttpStatus.UNAUTHORIZED, resource.likePost(1, null).getStatusCode());
        assertEquals(HttpStatus.UNAUTHORIZED, resource.unlikePost(1, null).getStatusCode());
    }

    @Test
    void analyticsEndpointsReturnServiceValues() {
        PostService postService = mock(PostService.class);
        PostResource resource = new PostResource(postService);
        PostDTO dto = PostDTO.builder().postId(1).build();
        when(postService.getMostViewedPosts(5)).thenReturn(List.of(dto));
        when(postService.getMostActiveAuthors(5)).thenReturn(List.of());
        when(postService.getTrendingTags(5)).thenReturn(List.of());

        assertEquals(1, resource.getMostViewedPosts(5).getBody().size());
        assertEquals(0, resource.getMostActiveAuthors(5).getBody().size());
        assertEquals(0, resource.getTrendingTags(5).getBody().size());
    }
}
