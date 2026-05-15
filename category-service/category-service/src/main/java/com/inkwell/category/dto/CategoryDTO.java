package com.inkwell.category.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for transferring Category data to/from clients.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CategoryDTO {

    private Integer categoryId;
    private String name;
    private String slug;
    private String description;
    private Integer parentCategoryId;
    private Integer postCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

