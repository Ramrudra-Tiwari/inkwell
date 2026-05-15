package com.inkwell.post.service.impl;

import com.inkwell.post.dto.CreatePostRequest;
import com.inkwell.post.dto.PostDTO;
import com.inkwell.post.dto.UpdatePostRequest;
import com.inkwell.post.client.CommentClient;
import com.inkwell.post.client.AuthClient;
import com.inkwell.post.client.MessagingClient;
import com.inkwell.post.client.NewsletterClient;
import com.inkwell.post.entity.Tag;
import com.inkwell.post.entity.Post;
import com.inkwell.post.entity.PostLike;
import com.inkwell.post.entity.PostStatus;
import com.inkwell.post.exception.PostNotFoundException;
import com.inkwell.post.mapper.PostMapper;
import com.inkwell.post.repository.AuditLogRepository;
import com.inkwell.post.repository.CategoryRepository;
import com.inkwell.post.repository.PostLikeRepository;
import com.inkwell.post.repository.PostRepository;
import com.inkwell.post.repository.PostViewRepository;
import com.inkwell.post.repository.TagRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class PostServiceImplTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private PostMapper postMapper;

    @Mock
    private CommentClient commentClient;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private TagRepository tagRepository;

    @Mock
    private AuthClient authClient;

    @Mock
    private MessagingClient messagingClient;

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private PostViewRepository postViewRepository;

    @Mock
    private PostLikeRepository postLikeRepository;

    @Mock
    private NewsletterClient newsletterClient;

    @InjectMocks
    private PostServiceImpl postService;

    private CreatePostRequest createPostRequest;
    private Post post;
    private PostDTO postDTO;

    @BeforeEach
    void setUp() {
        createPostRequest = CreatePostRequest.builder()
                .authorId(7)
                .title("Testing InkWell Post")
                .content("<p>InkWell testing content</p>")
                .excerpt("A short excerpt")
                .featuredImageUrl("https://cdn.inkwell.com/cover.jpg")
                .build();

        post = Post.builder()
                .postId(1)
                .authorId(7)
                .title("Testing InkWell Post")
                .slug("testing-inkwell-post")
                .content(createPostRequest.getContent())
                .excerpt(createPostRequest.getExcerpt())
                .featuredImageUrl(createPostRequest.getFeaturedImageUrl())
                .status(PostStatus.DRAFT)
                .readTimeMin(1)
                .viewCount(0)
                .likesCount(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        postDTO = PostDTO.builder()
                .postId(1)
                .authorId(7)
                .title("Testing InkWell Post")
                .slug("testing-inkwell-post")
                .content(createPostRequest.getContent())
                .excerpt(createPostRequest.getExcerpt())
                .featuredImageUrl(createPostRequest.getFeaturedImageUrl())
                .status(PostStatus.DRAFT)
                .readTimeMin(1)
                .viewCount(0)
                .likesCount(0)
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }

    @Test
    void createPost_successfullyCreatesDraftPost() {
        when(postRepository.existsBySlug("testing-inkwell-post")).thenReturn(false);
        when(postRepository.save(any(Post.class))).thenReturn(post);
        when(postMapper.toDTO(post)).thenReturn(postDTO);

        PostDTO result = postService.createPost(createPostRequest);

        ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);
        verify(postRepository).save(postCaptor.capture());

        Post savedPost = postCaptor.getValue();
        assertEquals("testing-inkwell-post", savedPost.getSlug());
        assertEquals(PostStatus.DRAFT, savedPost.getStatus());
        assertEquals(0, savedPost.getViewCount());
        assertEquals(0, savedPost.getLikesCount());
        assertTrue(savedPost.getReadTimeMin() >= 1);
        assertNotNull(result);
        assertEquals(1, result.getPostId());
        assertEquals("Testing InkWell Post", result.getTitle());
    }

    @Test
    void createPost_honorsPublishedStatus() {
        CreatePostRequest request = CreatePostRequest.builder()
                .authorId(7)
                .title("Published From Editor")
                .content("<p>Ready to publish now</p>")
                .excerpt("Ready now")
                .status(PostStatus.PUBLISHED)
                .build();

        when(postRepository.existsBySlug("published-from-editor")).thenReturn(false);
        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> {
            Post savedPost = invocation.getArgument(0);
            savedPost.setPostId(2);
            return savedPost;
        });
        when(postMapper.toDTO(any(Post.class))).thenReturn(PostDTO.builder()
                .postId(2)
                .authorId(7)
                .title("Published From Editor")
                .slug("published-from-editor")
                .status(PostStatus.PUBLISHED)
                .build());

        PostDTO result = postService.createPost(request);

        ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);
        verify(postRepository).save(postCaptor.capture());

        Post savedPost = postCaptor.getValue();
        assertEquals(PostStatus.PUBLISHED, savedPost.getStatus());
        assertNotNull(savedPost.getPublishedAt());
        assertEquals(PostStatus.PUBLISHED, result.getStatus());
        verify(newsletterClient).triggerNewsletterForNewPost("Published From Editor", "Ready now", 2);
    }

    @Test
    void getPostById_returnsMappedPostWhenRecordExists() {
        when(postRepository.findById(1)).thenReturn(Optional.of(post));
        when(postMapper.toDTO(post)).thenReturn(postDTO);
        when(commentClient.getCommentCountByPostId(1)).thenReturn(0);

        PostDTO result = postService.getPostById(1);

        assertNotNull(result);
        assertEquals(1, result.getPostId());
        assertEquals("testing-inkwell-post", result.getSlug());
    }

    @Test
    void getPostBySlug_mapsAndAddsCommentCount() {
        when(postRepository.findBySlug("testing-inkwell-post")).thenReturn(Optional.of(post));
        when(postMapper.toDTO(post)).thenReturn(postDTO);
        when(commentClient.getCommentCountByPostId(1)).thenReturn(3);

        PostDTO result = postService.getPostBySlug("testing-inkwell-post");

        assertEquals(3, result.getCommentCount());
    }

    @Test
    void getAllPosts_mapsRepositoryResultsAndAddsCommentCounts() {
        when(postRepository.findAll()).thenReturn(List.of(post));
        when(postMapper.toDTO(post)).thenReturn(postDTO);
        when(commentClient.getCommentCountByPostId(1)).thenReturn(2);

        List<PostDTO> results = postService.getAllPosts();

        assertEquals(1, results.size());
        assertEquals(2, results.get(0).getCommentCount());
    }

    @Test
    void getPublishedPosts_mapsRepositoryResults() {
        when(postRepository.findAllPublishedPosts()).thenReturn(List.of(post));
        when(postMapper.toDTO(post)).thenReturn(postDTO);

        assertEquals(1, postService.getPublishedPosts().size());
    }

    @Test
    void getPostsByAuthor_mapsRepositoryResults() {
        when(postRepository.findByAuthorId(7)).thenReturn(List.of(post));
        when(postMapper.toDTO(post)).thenReturn(postDTO);

        assertEquals(1, postService.getPostsByAuthor(7).size());
    }

    @Test
    void getPostsByAuthorAndStatus_mapsRepositoryResults() {
        when(postRepository.findByAuthorIdAndStatus(7, PostStatus.DRAFT)).thenReturn(List.of(post));
        when(postMapper.toDTO(post)).thenReturn(postDTO);

        assertEquals(1, postService.getPostsByAuthorAndStatus(7, PostStatus.DRAFT).size());
    }

    @Test
    void getPostById_throwsPostNotFoundExceptionWhenRecordDoesNotExist() {
        when(postRepository.findById(404)).thenReturn(Optional.empty());

        PostNotFoundException exception = assertThrows(
                PostNotFoundException.class,
                () -> postService.getPostById(404)
        );

        assertEquals("Post not found with ID: 404", exception.getMessage());
    }

    @Test
    void createPost_throwsWhenSlugAlreadyExists() {
        when(postRepository.existsBySlug("testing-inkwell-post")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> postService.createPost(createPostRequest));
    }

    @Test
    void updatePost_changesTitleContentAndStatus() {
        UpdatePostRequest request = UpdatePostRequest.builder()
                .title("Updated Title")
                .content("Updated content with several words")
                .excerpt("Updated excerpt")
                .featuredImageUrl("updated.png")
                .status(PostStatus.PUBLISHED)
                .build();
        when(postRepository.findById(1)).thenReturn(Optional.of(post));
        when(postRepository.existsBySlug("updated-title")).thenReturn(false);
        when(postRepository.save(post)).thenReturn(post);
        when(postMapper.toDTO(post)).thenReturn(postDTO);

        PostDTO result = postService.updatePost(1, request);

        assertNotNull(result);
        assertEquals("Updated Title", post.getTitle());
        assertEquals(PostStatus.PUBLISHED, post.getStatus());
        verify(postRepository).save(post);
    }

    @Test
    void updatePost_throwsWhenUpdatedSlugAlreadyExists() {
        UpdatePostRequest request = UpdatePostRequest.builder()
                .title("Updated Title")
                .build();
        when(postRepository.findById(1)).thenReturn(Optional.of(post));
        when(postRepository.existsBySlug("updated-title")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> postService.updatePost(1, request));
    }

    @Test
    void searchPosts_returnsEmptyListForBlankKeyword() {
        assertEquals(List.of(), postService.searchPosts("  "));
    }

    @Test
    void searchPosts_mapsRepositoryResults() {
        when(postRepository.searchByKeyword("spring")).thenReturn(List.of(post));
        when(postMapper.toDTO(post)).thenReturn(postDTO);

        List<PostDTO> results = postService.searchPosts("spring");

        assertEquals(1, results.size());
    }

    @Test
    void incrementViewCount_savesViewAndIncrementsFirstTimeSession() {
        when(postRepository.existsById(1)).thenReturn(true);
        when(postViewRepository.existsByPostIdAndSessionId(1, "session-1")).thenReturn(false);

        postService.incrementViewCount(1, "session-1");

        verify(postViewRepository).save(any());
        verify(postRepository).incrementViewCount(1);
    }

    @Test
    void incrementViewCount_doesNotIncrementWhenSessionAlreadyViewed() {
        when(postRepository.existsById(1)).thenReturn(true);
        when(postViewRepository.existsByPostIdAndSessionId(1, "session-1")).thenReturn(true);

        postService.incrementViewCount(1, "session-1");

        verify(postRepository, never()).incrementViewCount(1);
    }

    @Test
    void incrementViewCount_throwsWhenPostMissing() {
        when(postRepository.existsById(404)).thenReturn(false);

        assertThrows(PostNotFoundException.class, () -> postService.incrementViewCount(404, "session-1"));
    }

    @Test
    void incrementLikesCount_checksPostAndDelegates() {
        when(postRepository.existsById(1)).thenReturn(true);

        postService.incrementLikesCount(1);

        verify(postRepository).incrementLikesCount(1);
    }

    @Test
    void likePost_savesLikeAndIncrementsFirstTimeUser() {
        when(postRepository.existsById(1)).thenReturn(true);
        when(postLikeRepository.existsByPostIdAndUserId(1, 7)).thenReturn(false);

        boolean liked = postService.likePost(1, 7);

        assertTrue(liked);
        verify(postLikeRepository).save(any(PostLike.class));
        verify(postRepository).incrementLikesCount(1);
    }

    @Test
    void likePost_doesNotIncrementWhenUserAlreadyLiked() {
        when(postRepository.existsById(1)).thenReturn(true);
        when(postLikeRepository.existsByPostIdAndUserId(1, 7)).thenReturn(true);

        boolean liked = postService.likePost(1, 7);

        assertEquals(false, liked);
        verify(postLikeRepository, never()).save(any(PostLike.class));
        verify(postRepository, never()).incrementLikesCount(1);
    }

    @Test
    void likePost_throwsWhenPostMissing() {
        when(postRepository.existsById(404)).thenReturn(false);

        assertThrows(PostNotFoundException.class, () -> postService.likePost(404, 7));
    }

    @Test
    void unlikePost_deletesLikeAndDecrementsWhenPresent() {
        PostLike like = PostLike.builder().id(3L).postId(1).userId(7).build();
        when(postRepository.existsById(1)).thenReturn(true);
        when(postLikeRepository.findByPostIdAndUserId(1, 7)).thenReturn(Optional.of(like));

        boolean unliked = postService.unlikePost(1, 7);

        assertTrue(unliked);
        verify(postLikeRepository).delete(like);
        verify(postRepository).decrementLikesCount(1);
    }

    @Test
    void unlikePost_doesNotDecrementWhenLikeMissing() {
        when(postRepository.existsById(1)).thenReturn(true);
        when(postLikeRepository.findByPostIdAndUserId(1, 7)).thenReturn(Optional.empty());

        boolean unliked = postService.unlikePost(1, 7);

        assertEquals(false, unliked);
        verify(postRepository, never()).decrementLikesCount(1);
    }

    @Test
    void decrementLikesCount_checksPostAndDelegates() {
        when(postRepository.existsById(1)).thenReturn(true);

        postService.decrementLikesCount(1);

        verify(postRepository).decrementLikesCount(1);
    }

    @Test
    void decrementLikesCount_throwsWhenPostMissing() {
        when(postRepository.existsById(404)).thenReturn(false);

        assertThrows(PostNotFoundException.class, () -> postService.decrementLikesCount(404));
    }

    @Test
    void deletePost_removesPostAndComments() {
        when(postRepository.existsById(1)).thenReturn(true);

        postService.deletePost(1);

        verify(postLikeRepository).deleteByPostId(1);
        verify(postViewRepository).deleteByPostId(1);
        verify(postRepository).deleteTagLinksByPostId(1);
        verify(postRepository).deleteById(1);
        verify(commentClient).deleteCommentsByPostId(1);
        verify(auditLogRepository).save(any());
    }

    @Test
    void deletePost_throwsWhenPostMissing() {
        when(postRepository.existsById(404)).thenReturn(false);

        assertThrows(PostNotFoundException.class, () -> postService.deletePost(404));
    }

    @Test
    void deletePostsByAuthor_removesEveryPostAndComments() {
        Post secondPost = Post.builder().postId(2).authorId(7).title("Second").build();
        when(postRepository.findByAuthorId(7)).thenReturn(List.of(post, secondPost));

        postService.deletePostsByAuthor(7);

        verify(postLikeRepository).deleteByPostId(1);
        verify(postLikeRepository).deleteByPostId(2);
        verify(postViewRepository).deleteByPostId(1);
        verify(postViewRepository).deleteByPostId(2);
        verify(postRepository).deleteTagLinksByPostId(1);
        verify(postRepository).deleteTagLinksByPostId(2);
        verify(postRepository).deleteById(1);
        verify(postRepository).deleteById(2);
        verify(commentClient).deleteCommentsByPostId(1);
        verify(commentClient).deleteCommentsByPostId(2);
        verify(auditLogRepository).save(any());
    }

    @Test
    void publishPost_setsPublishedStatusAndTriggersNewsletter() {
        when(postRepository.findById(1)).thenReturn(Optional.of(post));
        when(postRepository.save(post)).thenReturn(post);
        when(postMapper.toDTO(post)).thenReturn(postDTO);

        PostDTO result = postService.publishPost(1);

        assertNotNull(result);
        assertEquals(PostStatus.PUBLISHED, post.getStatus());
        verify(newsletterClient).triggerNewsletterForNewPost(post.getTitle(), post.getExcerpt(), post.getPostId());
    }

    @Test
    void publishPost_notifiesFollowersWhenAvailable() {
        when(postRepository.findById(1)).thenReturn(Optional.of(post));
        when(postRepository.save(post)).thenReturn(post);
        when(postMapper.toDTO(post)).thenReturn(postDTO);
        when(authClient.getFollowerIds(7)).thenReturn(List.of(10, 11));

        postService.publishPost(1);

        verify(messagingClient).sendNotification(10, 7, "NEW_POST", "New Story Published",
                "A writer you follow just published: Testing InkWell Post");
        verify(messagingClient).sendNotification(11, 7, "NEW_POST", "New Story Published",
                "A writer you follow just published: Testing InkWell Post");
    }

    @Test
    void unpublishPost_setsUnpublishedStatus() {
        when(postRepository.findById(1)).thenReturn(Optional.of(post));
        when(postRepository.save(post)).thenReturn(post);
        when(postMapper.toDTO(post)).thenReturn(postDTO);

        postService.unpublishPost(1);

        assertEquals(PostStatus.UNPUBLISHED, post.getStatus());
    }

    @Test
    void featurePost_updatesFeatureFlagAndLogsAction() {
        when(postRepository.findById(1)).thenReturn(Optional.of(post));
        when(postRepository.save(post)).thenReturn(post);
        when(postMapper.toDTO(post)).thenReturn(postDTO);

        postService.featurePost(1, true);

        assertEquals(true, post.isFeatured());
        verify(auditLogRepository).save(any());
    }

    @Test
    void featurePost_canUnpinPost() {
        post.setFeatured(true);
        when(postRepository.findById(1)).thenReturn(Optional.of(post));
        when(postRepository.save(post)).thenReturn(post);
        when(postMapper.toDTO(post)).thenReturn(postDTO);

        postService.featurePost(1, false);

        assertEquals(false, post.isFeatured());
        verify(auditLogRepository).save(any());
    }

    @Test
    void getMostViewedPosts_mapsAndAddsCommentCounts() {
        when(postRepository.findTopMostViewedPosts(any())).thenReturn(List.of(post));
        when(postMapper.toDTO(post)).thenReturn(postDTO);
        when(commentClient.getCommentCountByPostId(1)).thenReturn(4);

        List<PostDTO> results = postService.getMostViewedPosts(5);

        assertEquals(1, results.size());
        assertEquals(4, results.get(0).getCommentCount());
    }

    @Test
    void getMostActiveAuthors_mapsAnalyticsRows() {
        when(postRepository.findMostActiveAuthors(any())).thenReturn(List.<Object[]>of(new Object[]{7, 3L}));

        var results = postService.getMostActiveAuthors(5);

        assertEquals(1, results.size());
        assertEquals(7, results.get(0).getAuthorId());
        assertEquals(3L, results.get(0).getPostCount());
    }

    @Test
    void getTrendingTags_mapsTags() {
        Tag tag = Tag.builder().tagId(2).name("Spring").slug("spring").build();
        when(tagRepository.findTrendingTags(any())).thenReturn(List.of(tag));

        var results = postService.getTrendingTags(5);

        assertEquals(1, results.size());
        assertEquals("Spring", results.get(0).getName());
    }
}
