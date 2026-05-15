package com.inkwell.post.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import com.inkwell.post.entity.PostStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for Post entity
 * Used for API responses to transfer post data without exposing internal details
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response DTO representing a blog post in InkWell.")
public class PostDTO {
    @Schema(description = "Unique identifier of the post.", example = "1")
    private Integer postId;
    @Schema(description = "Identifier of the author who owns the post.", example = "7")
    private Integer authorId;
    @Schema(description = "Human-readable post title.", example = "How InkWell Ships Features")
    private String title;
    @Schema(description = "SEO-friendly slug used in blog URLs.", example = "how-inkwell-ships-features")
    private String slug;
    @Schema(description = "Rich HTML content stored for the article body.", example = "<p>Hello InkWell.</p>")
    private String content;
    @Schema(description = "Short excerpt used in previews.", example = "A behind-the-scenes look at our workflow.")
    private String excerpt;
    @Schema(description = "Featured image URL shown in the post header.", example = "https://cdn.inkwell.local/posts/cover.jpg")
    private String featuredImageUrl;
    @Schema(description = "Current publishing status of the post.")
    private PostStatus status;
    @Schema(description = "Estimated read time in minutes.", example = "4")
    private Integer readTimeMin;
    @Schema(description = "Total number of recorded views.", example = "128")
    private Integer viewCount;
    @Schema(description = "Total number of likes.", example = "15")
    private Integer likesCount;
    @Schema(description = "Total number of comments.", example = "42")
    private Integer commentCount;
    @Schema(description = "Whether the post is pinned/featured on the platform.")
    private boolean isFeatured;
    @Schema(description = "Timestamp when the post was first created.")
    private LocalDateTime createdAt;
    @Schema(description = "Timestamp when the post was last updated.")
    private LocalDateTime updatedAt;
    @Schema(description = "Timestamp when the post was published. Null for drafts.")
    private LocalDateTime publishedAt;

    @Schema(description = "The category this post belongs to.")
    private CategoryDTO category;

    @Schema(description = "List of tags associated with this post.")
    private java.util.List<TagDTO> tags;
}

