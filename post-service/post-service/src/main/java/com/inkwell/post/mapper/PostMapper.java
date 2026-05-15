package com.inkwell.post.mapper;

import com.inkwell.post.dto.PostDTO;
import com.inkwell.post.entity.Post;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * MapStruct mapper for converting between Post entity and PostDTO
 * Automatically generates implementation for entity to DTO mapping
 */
@Mapper(componentModel = "spring", uses = TaxonomyMapper.class)
public interface PostMapper {

    PostMapper INSTANCE = Mappers.getMapper(PostMapper.class);

    /**
     * Convert Post entity to PostDTO
     *
     * @param post the Post entity
     * @return the PostDTO
     */
    @org.mapstruct.Mapping(target = "commentCount", ignore = true)
    PostDTO toDTO(Post post);

    /**
     * Convert PostDTO to Post entity
     *
     * @param postDTO the PostDTO
     * @return the Post entity
     */
    Post toEntity(PostDTO postDTO);
}

