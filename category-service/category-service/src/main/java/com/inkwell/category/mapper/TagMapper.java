package com.inkwell.category.mapper;

import com.inkwell.category.dto.TagDTO;
import com.inkwell.category.entity.Tag;
import org.mapstruct.Mapper;

/**
 * MapStruct mapper for Tag entity <-> TagDTO conversions.
 */
@Mapper(componentModel = "spring")
public interface TagMapper {

    /**
     * Convert Tag entity to DTO.
     */
    TagDTO toDTO(Tag tag);

    /**
     * Convert TagDTO to entity.
     */
    Tag toEntity(TagDTO dto);
}

