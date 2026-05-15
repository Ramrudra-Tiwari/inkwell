package com.inkwell.post.service;

import com.inkwell.post.dto.CategoryDTO;
import com.inkwell.post.dto.TagDTO;
import java.util.List;

public interface TaxonomyService {
    // Category management
    CategoryDTO createCategory(CategoryDTO categoryDTO);
    List<CategoryDTO> getAllCategories();
    List<CategoryDTO> getRootCategories();
    CategoryDTO getCategoryBySlug(String slug);
    void deleteCategory(Integer categoryId);

    // Tag management
    TagDTO createTag(TagDTO tagDTO);
    List<TagDTO> getAllTags();
    TagDTO getTagBySlug(String slug);
    void deleteTag(Integer tagId);
}
