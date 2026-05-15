package com.inkwell.category.service;

import com.inkwell.category.dto.TagDTO;
import com.inkwell.category.dto.TaxonomyRequest;
import com.inkwell.category.entity.Tag;
import com.inkwell.category.exception.TaxonomyNotFoundException;
import com.inkwell.category.mapper.TagMapper;
import com.inkwell.category.repository.TagRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tag Service Tests")
class TagServiceImplTest {

    @Mock
    private TagRepository tagRepository;

    @Mock
    private TagMapper tagMapper;

    @InjectMocks
    private TagServiceImpl tagService;

    private Tag tag;
    private TagDTO tagDTO;
    private TaxonomyRequest request;

    @BeforeEach
    void setUp() {
        tag = Tag.builder()
                .tagId(2)
                .name("Spring")
                .slug("spring")
                .postCount(3)
                .build();
        tagDTO = TagDTO.builder()
                .tagId(2)
                .name("Spring")
                .slug("spring")
                .postCount(3)
                .build();
        request = TaxonomyRequest.builder().name("Spring").build();
    }

    @Test
    @DisplayName("Should create tag when slug is available")
    void createTag_savesAndMapsTag() {
        when(tagRepository.existsBySlug("spring")).thenReturn(false);
        when(tagRepository.save(any(Tag.class))).thenReturn(tag);
        when(tagMapper.toDTO(tag)).thenReturn(tagDTO);

        TagDTO result = tagService.createTag(request);

        assertThat(result.getSlug()).isEqualTo("spring");
        verify(tagRepository).save(any(Tag.class));
    }

    @Test
    @DisplayName("Should reject duplicate tag slug")
    void createTag_throwsForDuplicateSlug() {
        when(tagRepository.existsBySlug("spring")).thenReturn(true);

        assertThatThrownBy(() -> tagService.createTag(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Tag slug already exists");
    }

    @Test
    @DisplayName("Should retrieve tags by id, slug, all, trending, and search")
    void readMethods_returnMappedTags() {
        when(tagRepository.findById(2)).thenReturn(Optional.of(tag));
        when(tagRepository.findBySlug("spring")).thenReturn(Optional.of(tag));
        when(tagRepository.findAll()).thenReturn(List.of(tag));
        when(tagRepository.findTopTrendingTags(5)).thenReturn(List.of(tag));
        when(tagRepository.searchByName("sp")).thenReturn(List.of(tag));
        when(tagMapper.toDTO(tag)).thenReturn(tagDTO);

        assertThat(tagService.getTagById(2).getTagId()).isEqualTo(2);
        assertThat(tagService.getTagBySlug("spring").getSlug()).isEqualTo("spring");
        assertThat(tagService.getAllTags()).hasSize(1);
        assertThat(tagService.getTrendingTags(5)).hasSize(1);
        assertThat(tagService.searchByName("sp")).hasSize(1);
    }

    @Test
    @DisplayName("Should update tag when name changes")
    void updateTag_changesNameAndSlug() {
        TaxonomyRequest updateRequest = TaxonomyRequest.builder().name("Spring Boot").build();
        Tag updated = Tag.builder().tagId(2).name("Spring Boot").slug("spring-boot").postCount(3).build();
        TagDTO updatedDTO = TagDTO.builder().tagId(2).name("Spring Boot").slug("spring-boot").postCount(3).build();
        when(tagRepository.findById(2)).thenReturn(Optional.of(tag));
        when(tagRepository.existsBySlug("spring-boot")).thenReturn(false);
        when(tagRepository.save(tag)).thenReturn(updated);
        when(tagMapper.toDTO(updated)).thenReturn(updatedDTO);

        TagDTO result = tagService.updateTag(2, updateRequest);

        assertThat(result.getSlug()).isEqualTo("spring-boot");
    }

    @Test
    @DisplayName("Should keep tag slug when name is unchanged")
    void updateTag_keepsExistingSlugWhenNameUnchanged() {
        when(tagRepository.findById(2)).thenReturn(Optional.of(tag));
        when(tagRepository.save(tag)).thenReturn(tag);
        when(tagMapper.toDTO(tag)).thenReturn(tagDTO);

        TagDTO result = tagService.updateTag(2, request);

        assertThat(result.getSlug()).isEqualTo("spring");
        verify(tagRepository, never()).existsBySlug(any());
    }

    @Test
    @DisplayName("Should reject update when new slug already exists")
    void updateTag_throwsWhenNewSlugExists() {
        TaxonomyRequest updateRequest = TaxonomyRequest.builder().name("Spring Boot").build();
        when(tagRepository.findById(2)).thenReturn(Optional.of(tag));
        when(tagRepository.existsBySlug("spring-boot")).thenReturn(true);

        assertThatThrownBy(() -> tagService.updateTag(2, updateRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Tag slug already exists");
        verify(tagRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw when tag is missing")
    void getTagById_throwsWhenMissing() {
        when(tagRepository.findById(404)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tagService.getTagById(404))
                .isInstanceOf(TaxonomyNotFoundException.class);
    }

    @Test
    @DisplayName("Should assign post association")
    void assignPostToTag_updatesPostCount() {
        when(tagRepository.findById(2)).thenReturn(Optional.of(tag));

        tagService.assignPostToTag(2, 99);

        verify(tagRepository).incrementPostCount(2);
    }

    @Test
    @DisplayName("Should remove post association")
    void removePostFromTag_updatesPostCount() {
        when(tagRepository.findById(2)).thenReturn(Optional.of(tag));

        tagService.removePostFromTag(2, 99);

        verify(tagRepository).decrementPostCount(2);
    }
}
