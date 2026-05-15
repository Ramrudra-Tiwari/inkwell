package com.inkwell.post.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDTO {
    private Integer categoryId;
    private String name;
    private String slug;
    private String description;
    private Integer parentCategoryId;
    private List<CategoryDTO> subCategories;
}
