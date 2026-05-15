package com.inkwell.auth.mapper;

import com.inkwell.auth.dto.RegisterRequest;
import com.inkwell.auth.dto.UserDTO;
import com.inkwell.auth.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper for converting between User entity and DTOs.
 */
@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDTO toDTO(User user);

    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "role", constant = "AUTHOR")
    @Mapping(target = "provider", constant = "LOCAL")
    @Mapping(target = "isActive", constant = "true")
    @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "bio", ignore = true)
    @Mapping(target = "avatarUrl", ignore = true)
    User toEntity(RegisterRequest request);
}
