package com.inkwell.post.controller;

import com.inkwell.post.dto.CategoryDTO;
import com.inkwell.post.dto.TagDTO;
import com.inkwell.post.service.TaxonomyService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TaxonomyControllerTest {

    @Test
    void categoryEndpointsDelegateToService() {
        TaxonomyService taxonomyService = mock(TaxonomyService.class);
        TaxonomyController controller = new TaxonomyController(taxonomyService);
        CategoryDTO category = CategoryDTO.builder().categoryId(1).name("Tech").build();
        when(taxonomyService.createCategory(category)).thenReturn(category);
        when(taxonomyService.getAllCategories()).thenReturn(List.of(category));
        when(taxonomyService.getRootCategories()).thenReturn(List.of(category));

        assertEquals(category, controller.createCategory(category).getBody());
        assertEquals(1, controller.getAllCategories().getBody().size());
        assertEquals(1, controller.getRootCategories().getBody().size());
        assertEquals(HttpStatus.OK, controller.deleteCategory(1).getStatusCode());
        verify(taxonomyService).deleteCategory(1);
    }

    @Test
    void tagEndpointsDelegateToService() {
        TaxonomyService taxonomyService = mock(TaxonomyService.class);
        TaxonomyController controller = new TaxonomyController(taxonomyService);
        TagDTO tag = TagDTO.builder().tagId(1).name("Java").build();
        when(taxonomyService.createTag(tag)).thenReturn(tag);
        when(taxonomyService.getAllTags()).thenReturn(List.of(tag));

        assertEquals(tag, controller.createTag(tag).getBody());
        assertEquals(1, controller.getAllTags().getBody().size());
        assertEquals(HttpStatus.OK, controller.deleteTag(1).getStatusCode());
        verify(taxonomyService).deleteTag(1);
    }
}
