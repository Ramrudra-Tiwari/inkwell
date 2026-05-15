package com.inkwell.comment.mapper;

import com.inkwell.comment.dto.CommentDTO;
import com.inkwell.comment.entity.Comment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    CommentDTO toDTO(Comment comment);

    @Mapping(target = "commentId", ignore = true)
    @Mapping(target = "likesCount", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Comment toEntity(CommentDTO commentDTO);
}
