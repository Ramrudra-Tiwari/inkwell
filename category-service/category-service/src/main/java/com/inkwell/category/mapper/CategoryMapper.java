package com.inkwell.category.mapper;

import com.inkwell.category.dto.CategoryDTO;
import com.inkwell.category.entity.Category;
import org.mapstruct.Mapper;

/**
 * MapStruct mapper for Category entity <-> CategoryDTO conversions.
 */
@Mapper(componentModel = "spring")
public interface CategoryMapper {

    /**
     * Convert Category entity to DTO.
     */
    CategoryDTO toDTO(Category category);

    /**
     * Convert CategoryDTO to entity.
     */
    Category toEntity(CategoryDTO dto);
}

