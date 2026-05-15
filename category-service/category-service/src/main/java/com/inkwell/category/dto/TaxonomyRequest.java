package com.inkwell.category.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating/updating categories and tags.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaxonomyRequest {

    @NotBlank(message = "Name is required")
    private String name;

    private String description; // For categories only

    private Integer parentCategoryId; // For categories only
}

