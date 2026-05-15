package com.inkwell.post.controller;

import com.inkwell.post.dto.CategoryDTO;
import com.inkwell.post.dto.TagDTO;
import com.inkwell.post.service.TaxonomyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/taxonomy")
@RequiredArgsConstructor
@Tag(name = "Taxonomy Management", description = "APIs for managing categories and tags")
public class TaxonomyController {

    private final TaxonomyService taxonomyService;

    @PostMapping("/categories")
    @Operation(summary = "Create a new category")
    public ResponseEntity<CategoryDTO> createCategory(@RequestBody CategoryDTO categoryDTO) {
        return ResponseEntity.ok(taxonomyService.createCategory(categoryDTO));
    }

    @GetMapping("/categories")
    @Operation(summary = "Get all categories")
    public ResponseEntity<List<CategoryDTO>> getAllCategories() {
        return ResponseEntity.ok(taxonomyService.getAllCategories());
    }

    @GetMapping("/categories/roots")
    @Operation(summary = "Get root categories")
    public ResponseEntity<List<CategoryDTO>> getRootCategories() {
        return ResponseEntity.ok(taxonomyService.getRootCategories());
    }

    @DeleteMapping("/categories/{categoryId}")
    @Operation(summary = "Delete a category")
    public ResponseEntity<Void> deleteCategory(@PathVariable Integer categoryId) {
        taxonomyService.deleteCategory(categoryId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/tags")
    @Operation(summary = "Create a new tag")
    public ResponseEntity<TagDTO> createTag(@RequestBody TagDTO tagDTO) {
        return ResponseEntity.ok(taxonomyService.createTag(tagDTO));
    }

    @GetMapping("/tags")
    @Operation(summary = "Get all tags")
    public ResponseEntity<List<TagDTO>> getAllTags() {
        return ResponseEntity.ok(taxonomyService.getAllTags());
    }

    @DeleteMapping("/tags/{tagId}")
    @Operation(summary = "Delete a tag")
    public ResponseEntity<Void> deleteTag(@PathVariable Integer tagId) {
        taxonomyService.deleteTag(tagId);
        return ResponseEntity.ok().build();
    }
}
