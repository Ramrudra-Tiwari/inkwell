package com.inkwell.post.mapper;

import com.inkwell.post.dto.CategoryDTO;
import com.inkwell.post.dto.TagDTO;
import com.inkwell.post.entity.Category;
import com.inkwell.post.entity.Tag;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TaxonomyMapper {

    @Mapping(target = "parentCategoryId", source = "parentCategory.categoryId")
    CategoryDTO toCategoryDTO(Category category);

    @Mapping(target = "parentCategory", ignore = true) // Will be handled in service
    Category toCategoryEntity(CategoryDTO categoryDTO);

    TagDTO toTagDTO(Tag tag);

    Tag toTagEntity(TagDTO tagDTO);
}
