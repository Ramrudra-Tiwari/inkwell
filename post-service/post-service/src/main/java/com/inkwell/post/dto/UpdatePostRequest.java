package com.inkwell.post.dto;

import com.inkwell.post.entity.PostStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating an existing post
 * Allows updating title, content, excerpt, featured image, and status
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePostRequest {

    private String title; // Optional

    private String content; // Optional - Rich HTML content

    private String excerpt; // Optional

    private String featuredImageUrl; // Optional

    private PostStatus status; // Optional - Can change post status

    private Integer categoryId; // Optional

    private java.util.List<Integer> tagIds; // Optional

    /**
     * Validation note:
     * - If title is provided, slug will be regenerated
     * - If status changes to PUBLISHED, publishedAt will be set
     */
}

