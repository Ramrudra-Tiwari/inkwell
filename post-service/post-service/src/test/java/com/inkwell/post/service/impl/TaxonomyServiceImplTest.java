package com.inkwell.post.service.impl;

import com.inkwell.post.dto.CategoryDTO;
import com.inkwell.post.dto.TagDTO;
import com.inkwell.post.entity.Category;
import com.inkwell.post.entity.Tag;
import com.inkwell.post.mapper.TaxonomyMapper;
import com.inkwell.post.repository.CategoryRepository;
import com.inkwell.post.repository.TagRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaxonomyServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private TagRepository tagRepository;

    @Mock
    private TaxonomyMapper taxonomyMapper;

    @InjectMocks
    private TaxonomyServiceImpl taxonomyService;

    @Test
    void createCategory_generatesSlugAndMapsSavedCategory() {
        CategoryDTO request = CategoryDTO.builder().name("Tech News").build();
        Category category = Category.builder().name("Tech News").build();
        Category saved = Category.builder().categoryId(1).name("Tech News").slug("tech-news").build();
        CategoryDTO response = CategoryDTO.builder().categoryId(1).name("Tech News").slug("tech-news").build();
        when(taxonomyMapper.toCategoryEntity(request)).thenReturn(category);
        when(categoryRepository.save(category)).thenReturn(saved);
        when(taxonomyMapper.toCategoryDTO(saved)).thenReturn(response);

        assertEquals("tech-news", taxonomyService.createCategory(request).getSlug());
    }

    @Test
    void createCategory_usesParentWhenProvided() {
        CategoryDTO request = CategoryDTO.builder().name("Java").parentCategoryId(1).build();
        Category category = Category.builder().name("Java").build();
        Category parent = Category.builder().categoryId(1).name("Tech").build();
        when(taxonomyMapper.toCategoryEntity(request)).thenReturn(category);
        when(categoryRepository.findById(1)).thenReturn(Optional.of(parent));
        when(categoryRepository.save(category)).thenReturn(category);
        when(taxonomyMapper.toCategoryDTO(category)).thenReturn(CategoryDTO.builder().name("Java").build());

        taxonomyService.createCategory(request);

        assertEquals(parent, category.getParentCategory());
    }

    @Test
    void createCategory_throwsWhenParentMissing() {
        CategoryDTO request = CategoryDTO.builder().name("Java").parentCategoryId(1).build();
        Category category = Category.builder().name("Java").build();
        when(taxonomyMapper.toCategoryEntity(request)).thenReturn(category);
        when(categoryRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> taxonomyService.createCategory(request));
    }

    @Test
    void categoryReadAndDeleteMethodsDelegateToRepository() {
        Category category = Category.builder().categoryId(1).name("Tech").slug("tech").build();
        CategoryDTO dto = CategoryDTO.builder().categoryId(1).name("Tech").slug("tech").build();
        when(categoryRepository.findAll()).thenReturn(List.of(category));
        when(categoryRepository.findRootCategories()).thenReturn(List.of(category));
        when(categoryRepository.findBySlug("tech")).thenReturn(Optional.of(category));
        when(taxonomyMapper.toCategoryDTO(category)).thenReturn(dto);

        assertEquals(1, taxonomyService.getAllCategories().size());
        assertEquals(1, taxonomyService.getRootCategories().size());
        assertEquals(dto, taxonomyService.getCategoryBySlug("tech"));
        taxonomyService.deleteCategory(1);
        verify(categoryRepository).deleteById(1);
    }

    @Test
    void tagMethodsCreateReadAndDeleteTags() {
        TagDTO request = TagDTO.builder().name("Spring Boot").build();
        Tag tag = Tag.builder().name("Spring Boot").build();
        Tag saved = Tag.builder().tagId(1).name("Spring Boot").slug("spring-boot").build();
        TagDTO response = TagDTO.builder().tagId(1).name("Spring Boot").slug("spring-boot").build();
        when(taxonomyMapper.toTagEntity(request)).thenReturn(tag);
        when(tagRepository.save(tag)).thenReturn(saved);
        when(taxonomyMapper.toTagDTO(saved)).thenReturn(response);

        assertEquals("spring-boot", taxonomyService.createTag(request).getSlug());

        when(tagRepository.findAll()).thenReturn(List.of(saved));
        when(tagRepository.findBySlug("spring-boot")).thenReturn(Optional.of(saved));
        assertEquals(1, taxonomyService.getAllTags().size());
        assertEquals(response, taxonomyService.getTagBySlug("spring-boot"));
        taxonomyService.deleteTag(1);
        verify(tagRepository).deleteById(1);
    }
}
