package com.inkwell.media.mapper;

import com.inkwell.media.dto.MediaDTO;
import com.inkwell.media.entity.Media;
import org.mapstruct.Mapper;

/**
 * MapStruct mapper for converting between Media entity and MediaDTO.
 * Handles entity-to-DTO and DTO-to-entity transformations.
 */
@Mapper(componentModel = "spring")
public interface MediaMapper {

    /**
     * Convert a Media entity to MediaDTO.
     *
     * @param media the media entity
     * @return the corresponding DTO
     */
    MediaDTO toDTO(Media media);

    /**
     * Convert a MediaDTO to Media entity.
     *
     * @param mediaDTO the media DTO
     * @return the corresponding entity
     */
    Media toEntity(MediaDTO mediaDTO);
}

