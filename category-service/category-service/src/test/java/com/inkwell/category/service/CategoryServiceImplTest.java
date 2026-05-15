package com.inkwell.category.service;

import com.inkwell.category.dto.CategoryDTO;
import com.inkwell.category.dto.TaxonomyRequest;
import com.inkwell.category.entity.Category;
import com.inkwell.category.exception.TaxonomyNotFoundException;
import com.inkwell.category.mapper.CategoryMapper;
import com.inkwell.category.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CategoryServiceImpl.
 * Focuses on parent-child hierarchy logic and postCount increment/decrement logic.
 */
@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private Category testCategory;
    private CategoryDTO testCategoryDTO;
    private TaxonomyRequest createRequest;

    @BeforeEach
    void setUp() {
        // Setup test data
        testCategory = Category.builder()
                .categoryId(1)
                .name("Programming")
                .slug("programming")
                .description("Programming category")
                .parentCategoryId(null)
                .postCount(5)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        testCategoryDTO = CategoryDTO.builder()
                .categoryId(1)
                .name("Programming")
                .slug("programming")
                .description("Programming category")
                .parentCategoryId(null)
                .postCount(5)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        createRequest = TaxonomyRequest.builder()
                .name("Programming")
                .description("Programming category")
                .parentCategoryId(null)
                .build();
    }

    // ==========================================
    // HIERARCHY VALIDATION TESTS
    // ==========================================

    @Test
    void testValidateHierarchy_ValidHierarchy_ShouldReturnTrue() {
        // Given
        Integer categoryId = 1;
        Integer parentCategoryId = 2;

        Category parentCategory = Category.builder()
                .categoryId(2)
                .name("Technology")
                .parentCategoryId(null)
                .build();

        when(categoryRepository.findById(2)).thenReturn(Optional.of(parentCategory));

        // When
        boolean result = categoryService.validateHierarchy(categoryId, parentCategoryId);

        // Then
        assertThat(result).isTrue();
        verify(categoryRepository).findById(2);
    }

    @Test
    void testValidateHierarchy_SelfReference_ShouldReturnFalse() {
        // Given
        Integer categoryId = 1;
        Integer parentCategoryId = 1; // Same as categoryId

        // When
        boolean result = categoryService.validateHierarchy(categoryId, parentCategoryId);

        // Then
        assertThat(result).isFalse();
        verifyNoInteractions(categoryRepository);
    }

    @Test
    void testValidateHierarchy_CircularReference_ShouldReturnFalse() {
        // Given: A -> B -> C -> A (circular)
        Integer categoryId = 1; // A
        Integer parentCategoryId = 2; // B

        Category categoryB = Category.builder()
                .categoryId(2)
                .parentCategoryId(3)
                .build();

        Category categoryC = Category.builder()
                .categoryId(3)
                .parentCategoryId(1) // Points back to A
                .build();

        when(categoryRepository.findById(2)).thenReturn(Optional.of(categoryB));
        when(categoryRepository.findById(3)).thenReturn(Optional.of(categoryC));

        // When
        boolean result = categoryService.validateHierarchy(categoryId, parentCategoryId);

        // Then
        assertThat(result).isFalse();
        verify(categoryRepository, times(2)).findById(any(Integer.class));
    }

    @Test
    void testValidateHierarchy_DeepHierarchy_Valid() {
        // Given: A -> B -> C -> D (valid chain)
        Integer categoryId = 1; // A
        Integer parentCategoryId = 2; // B

        Category categoryB = Category.builder()
                .categoryId(2)
                .parentCategoryId(3)
                .build();

        Category categoryC = Category.builder()
                .categoryId(3)
                .parentCategoryId(4)
                .build();

        Category categoryD = Category.builder()
                .categoryId(4)
                .parentCategoryId(null) // Root category
                .build();

        when(categoryRepository.findById(2)).thenReturn(Optional.of(categoryB));
        when(categoryRepository.findById(3)).thenReturn(Optional.of(categoryC));
        when(categoryRepository.findById(4)).thenReturn(Optional.of(categoryD));

        // When
        boolean result = categoryService.validateHierarchy(categoryId, parentCategoryId);

        // Then
        assertThat(result).isTrue();
        verify(categoryRepository, times(3)).findById(any(Integer.class));
    }

    // ==========================================
    // POST COUNT INCREMENT/DECREMENT TESTS
    // ==========================================

    @Test
    void testAssignPostToCategory_Success() {
        // Given
        Integer categoryId = 1;
        Integer postId = 100;

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(testCategory));
        doNothing().when(categoryRepository).incrementPostCount(categoryId);

        // When
        categoryService.assignPostToCategory(categoryId, postId);

        // Then
        verify(categoryRepository).findById(categoryId);
        verify(categoryRepository).incrementPostCount(categoryId);
    }

    @Test
    void testAssignPostToCategory_CategoryNotFound() {
        // Given
        Integer categoryId = 999;
        Integer postId = 100;

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> categoryService.assignPostToCategory(categoryId, postId))
                .isInstanceOf(TaxonomyNotFoundException.class)
                .hasMessage("Category not found with ID: 999");

        verify(categoryRepository).findById(categoryId);
        verify(categoryRepository, never()).incrementPostCount(any(Integer.class));
    }

    @Test
    void testRemovePostFromCategory_Success() {
        // Given
        Integer categoryId = 1;
        Integer postId = 100;

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(testCategory));
        doNothing().when(categoryRepository).decrementPostCount(categoryId);

        // When
        categoryService.removePostFromCategory(categoryId, postId);

        // Then
        verify(categoryRepository).findById(categoryId);
        verify(categoryRepository).decrementPostCount(categoryId);
    }

    @Test
    void testRemovePostFromCategory_CategoryNotFound() {
        // Given
        Integer categoryId = 999;
        Integer postId = 100;

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> categoryService.removePostFromCategory(categoryId, postId))
                .isInstanceOf(TaxonomyNotFoundException.class)
                .hasMessage("Category not found with ID: 999");

        verify(categoryRepository).findById(categoryId);
        verify(categoryRepository, never()).decrementPostCount(any(Integer.class));
    }

    // ==========================================
    // CREATE CATEGORY WITH HIERARCHY TESTS
    // ==========================================

    @Test
    void testCreateCategory_WithValidParent_Success() {
        // Given
        TaxonomyRequest request = TaxonomyRequest.builder()
                .name("Java Programming")
                .description("Java programming tutorials")
                .parentCategoryId(1) // Parent exists
                .build();

        Category parentCategory = Category.builder()
                .categoryId(1)
                .name("Programming")
                .build();

        Category createdCategory = Category.builder()
                .categoryId(2)
                .name("Java Programming")
                .slug("java-programming")
                .description("Java programming tutorials")
                .parentCategoryId(1)
                .postCount(0)
                .build();

        when(categoryRepository.existsBySlug("java-programming")).thenReturn(false);
        when(categoryRepository.findById(1)).thenReturn(Optional.of(parentCategory));
        when(categoryRepository.save(any(Category.class))).thenReturn(createdCategory);
        when(categoryMapper.toDTO(createdCategory)).thenReturn(CategoryDTO.builder()
                .categoryId(2)
                .name("Java Programming")
                .slug("java-programming")
                .description("Java programming tutorials")
                .parentCategoryId(1)
                .postCount(0)
                .build());

        // When
        CategoryDTO result = categoryService.createCategory(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Java Programming");
        assertThat(result.getSlug()).isEqualTo("java-programming");
        assertThat(result.getParentCategoryId()).isEqualTo(1);
        assertThat(result.getPostCount()).isEqualTo(0);

        verify(categoryRepository).existsBySlug("java-programming");
        verify(categoryRepository).findById(1);
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void testCreateCategory_WithInvalidParent_ThrowsException() {
        // Given
        TaxonomyRequest request = TaxonomyRequest.builder()
                .name("Java Programming")
                .parentCategoryId(999) // Parent doesn't exist
                .build();

        when(categoryRepository.existsBySlug("java-programming")).thenReturn(false);
        when(categoryRepository.findById(999)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> categoryService.createCategory(request))
                .isInstanceOf(TaxonomyNotFoundException.class)
                .hasMessage("Category not found with ID: 999");

        verify(categoryRepository).existsBySlug("java-programming");
        verify(categoryRepository).findById(999);
        verify(categoryRepository, never()).save(any(Category.class));
    }

    // ==========================================
    // UPDATE CATEGORY HIERARCHY TESTS
    // ==========================================

    @Test
    void testUpdateCategory_ChangeParent_ValidHierarchy() {
        // Given
        Integer categoryId = 2;
        TaxonomyRequest updateRequest = TaxonomyRequest.builder()
                .name("Java Programming") // Same name
                .description("Updated description")
                .parentCategoryId(3) // New parent
                .build();

        Category existingCategory = Category.builder()
                .categoryId(2)
                .name("Java Programming")
                .slug("java-programming")
                .description("Old description")
                .parentCategoryId(1) // Old parent
                .build();

        Category updatedCategory = Category.builder()
                .categoryId(2)
                .name("Java Programming")
                .slug("java-programming")
                .description("Updated description")
                .parentCategoryId(3) // New parent
                .build();

        when(categoryRepository.findById(2)).thenReturn(Optional.of(existingCategory));
        when(categoryRepository.save(any(Category.class))).thenReturn(updatedCategory);
        when(categoryMapper.toDTO(updatedCategory)).thenReturn(CategoryDTO.builder()
                .categoryId(2)
                .name("Java Programming")
                .slug("java-programming")
                .description("Updated description")
                .parentCategoryId(3)
                .build());

        // When
        CategoryDTO result = categoryService.updateCategory(categoryId, updateRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getDescription()).isEqualTo("Updated description");
        assertThat(result.getParentCategoryId()).isEqualTo(3);

        verify(categoryRepository).findById(2);
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void testUpdateCategory_ChangeParent_InvalidHierarchy() {
        // Given
        Integer categoryId = 2;
        TaxonomyRequest updateRequest = TaxonomyRequest.builder()
                .name("Java Programming")
                .parentCategoryId(2) // Self-reference
                .build();

        Category existingCategory = Category.builder()
                .categoryId(2)
                .name("Java Programming")
                .slug("java-programming")
                .parentCategoryId(1)
                .build();

        when(categoryRepository.findById(2)).thenReturn(Optional.of(existingCategory));

        // When & Then
        assertThatThrownBy(() -> categoryService.updateCategory(categoryId, updateRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid parent category: creates circular reference");

        verify(categoryRepository).findById(2);
        verify(categoryRepository, never()).save(any(Category.class));
    }

    // ==========================================
    // CHILD CATEGORIES TESTS
    // ==========================================

    @Test
    void testGetChildCategories_Success() {
        // Given
        Integer parentCategoryId = 1;

        Category parentCategory = Category.builder()
                .categoryId(1)
                .name("Programming")
                .build();

        List<Category> childCategories = Arrays.asList(
                Category.builder().categoryId(2).name("Java").parentCategoryId(1).build(),
                Category.builder().categoryId(3).name("Python").parentCategoryId(1).build()
        );

        List<CategoryDTO> childCategoryDTOs = Arrays.asList(
                CategoryDTO.builder().categoryId(2).name("Java").parentCategoryId(1).build(),
                CategoryDTO.builder().categoryId(3).name("Python").parentCategoryId(1).build()
        );

        when(categoryRepository.findById(1)).thenReturn(Optional.of(parentCategory));
        when(categoryRepository.findByParentCategoryId(1)).thenReturn(childCategories);
        when(categoryMapper.toDTO(childCategories.get(0))).thenReturn(childCategoryDTOs.get(0));
        when(categoryMapper.toDTO(childCategories.get(1))).thenReturn(childCategoryDTOs.get(1));

        // When
        List<CategoryDTO> result = categoryService.getChildCategories(parentCategoryId);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Java");
        assertThat(result.get(1).getName()).isEqualTo("Python");

        verify(categoryRepository).findById(1);
        verify(categoryRepository).findByParentCategoryId(1);
    }

    @Test
    void testGetChildCategories_ParentNotFound() {
        // Given
        Integer parentCategoryId = 999;

        when(categoryRepository.findById(999)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> categoryService.getChildCategories(parentCategoryId))
                .isInstanceOf(TaxonomyNotFoundException.class)
                .hasMessage("Category not found with ID: 999");

        verify(categoryRepository).findById(999);
        verify(categoryRepository, never()).findByParentCategoryId(any(Integer.class));
    }
}

