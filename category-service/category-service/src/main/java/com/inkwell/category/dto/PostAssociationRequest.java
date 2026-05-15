package com.inkwell.category.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for associating posts with categories/tags.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostAssociationRequest {

    @NotNull(message = "Post ID is required")
    private Integer postId;

    // For category association
    private Integer categoryId;

    // For tag association
    private Integer tagId;
}

