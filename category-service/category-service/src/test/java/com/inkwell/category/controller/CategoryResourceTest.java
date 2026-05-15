package com.inkwell.category.controller;

import com.inkwell.category.dto.CategoryDTO;
import com.inkwell.category.dto.PostAssociationRequest;
import com.inkwell.category.dto.TagDTO;
import com.inkwell.category.dto.TaxonomyRequest;
import com.inkwell.category.service.CategoryService;
import com.inkwell.category.service.TagService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Category Resource Tests")
class CategoryResourceTest {

    @Mock
    private CategoryService categoryService;

    @Mock
    private TagService tagService;

    private CategoryResource resource;
    private TaxonomyRequest request;
    private CategoryDTO category;
    private TagDTO tag;

    @BeforeEach
    void setUp() {
        resource = new CategoryResource(categoryService, tagService);
        request = TaxonomyRequest.builder().name("Java").description("Java posts").build();
        category = CategoryDTO.builder().categoryId(1).name("Java").slug("java").postCount(1).build();
        tag = TagDTO.builder().tagId(2).name("Spring").slug("spring").postCount(3).build();
    }

    @Test
    @DisplayName("Should delegate category read and write endpoints")
    void categoryEndpoints_delegateToService() {
        PostAssociationRequest association = PostAssociationRequest.builder().postId(99).build();
        when(categoryService.createCategory(request)).thenReturn(category);
        when(categoryService.getCategoryById(1)).thenReturn(category);
        when(categoryService.getCategoryBySlug("java")).thenReturn(category);
        when(categoryService.getAllCategories()).thenReturn(List.of(category));
        when(categoryService.getTopLevelCategories()).thenReturn(List.of(category));
        when(categoryService.getChildCategories(1)).thenReturn(List.of(category));
        when(categoryService.updateCategory(1, request)).thenReturn(category);
        when(categoryService.searchByName("ja")).thenReturn(List.of(category));

        assertEquals(HttpStatus.CREATED, resource.createCategory(request).getStatusCode());
        assertEquals(1, resource.getCategoryById(1).getBody().getCategoryId());
        assertEquals("java", resource.getCategoryBySlug("java").getBody().getSlug());
        assertEquals(1, resource.getAllCategories().getBody().size());
        assertEquals(1, resource.getTopLevelCategories().getBody().size());
        assertEquals(1, resource.getChildCategories(1).getBody().size());
        assertEquals("Java", resource.updateCategory(1, request).getBody().getName());
        assertEquals(1, resource.searchCategories("ja").getBody().size());
        assertEquals(HttpStatus.NO_CONTENT, resource.assignPostToCategory(1, association).getStatusCode());
        assertEquals(HttpStatus.NO_CONTENT, resource.removePostFromCategory(1, 99).getStatusCode());
        assertEquals(HttpStatus.NO_CONTENT, resource.deleteCategory(1).getStatusCode());

        verify(categoryService).assignPostToCategory(1, 99);
        verify(categoryService).removePostFromCategory(1, 99);
        verify(categoryService).deleteCategory(1);
    }

    @Test
    @DisplayName("Should delegate tag read and write endpoints")
    void tagEndpoints_delegateToService() {
        PostAssociationRequest association = PostAssociationRequest.builder().postId(99).build();
        when(tagService.createTag(request)).thenReturn(tag);
        when(tagService.getTagById(2)).thenReturn(tag);
        when(tagService.getTagBySlug("spring")).thenReturn(tag);
        when(tagService.getAllTags()).thenReturn(List.of(tag));
        when(tagService.getTrendingTags(5)).thenReturn(List.of(tag));
        when(tagService.updateTag(2, request)).thenReturn(tag);
        when(tagService.searchByName("sp")).thenReturn(List.of(tag));

        assertEquals(HttpStatus.CREATED, resource.createTag(request).getStatusCode());
        assertEquals(2, resource.getTagById(2).getBody().getTagId());
        assertEquals("spring", resource.getTagBySlug("spring").getBody().getSlug());
        assertEquals(1, resource.getAllTags().getBody().size());
        assertEquals(1, resource.getTrendingTags(5).getBody().size());
        assertEquals("Spring", resource.updateTag(2, request).getBody().getName());
        assertEquals(1, resource.searchTags("sp").getBody().size());
        assertEquals(HttpStatus.NO_CONTENT, resource.assignPostToTag(2, association).getStatusCode());
        assertEquals(HttpStatus.NO_CONTENT, resource.removePostFromTag(2, 99).getStatusCode());
        assertEquals(HttpStatus.NO_CONTENT, resource.deleteTag(2).getStatusCode());

        verify(tagService).assignPostToTag(2, 99);
        verify(tagService).removePostFromTag(2, 99);
        verify(tagService).deleteTag(2);
    }
}
