package com.inkwell.post.service.impl;

import com.inkwell.post.dto.CategoryDTO;
import com.inkwell.post.dto.TagDTO;
import com.inkwell.post.entity.Category;
import com.inkwell.post.entity.Tag;
import com.inkwell.post.mapper.TaxonomyMapper;
import com.inkwell.post.repository.CategoryRepository;
import com.inkwell.post.repository.TagRepository;
import com.inkwell.post.service.TaxonomyService;
import com.inkwell.post.util.SlugUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TaxonomyServiceImpl implements TaxonomyService {

    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final TaxonomyMapper taxonomyMapper;

    @Override
    public CategoryDTO createCategory(CategoryDTO categoryDTO) {
        log.info("Creating category: {}", categoryDTO.getName());
        Category category = taxonomyMapper.toCategoryEntity(categoryDTO);
        
        if (categoryDTO.getSlug() == null || categoryDTO.getSlug().isEmpty()) {
            category.setSlug(SlugUtil.toSlug(categoryDTO.getName()));
        }

        if (categoryDTO.getParentCategoryId() != null) {
            Category parent = categoryRepository.findById(categoryDTO.getParentCategoryId())
                    .orElseThrow(() -> new RuntimeException("Parent category not found"));
            category.setParentCategory(parent);
        }

        Category saved = categoryRepository.save(category);
        return taxonomyMapper.toCategoryDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDTO> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(taxonomyMapper::toCategoryDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDTO> getRootCategories() {
        return categoryRepository.findRootCategories().stream()
                .map(taxonomyMapper::toCategoryDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryDTO getCategoryBySlug(String slug) {
        Category category = categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        return taxonomyMapper.toCategoryDTO(category);
    }

    @Override
    public void deleteCategory(Integer categoryId) {
        log.info("Deleting category: {}", categoryId);
        categoryRepository.deleteById(categoryId);
    }

    @Override
    public TagDTO createTag(TagDTO tagDTO) {
        log.info("Creating tag: {}", tagDTO.getName());
        Tag tag = taxonomyMapper.toTagEntity(tagDTO);
        
        if (tagDTO.getSlug() == null || tagDTO.getSlug().isEmpty()) {
            tag.setSlug(SlugUtil.toSlug(tagDTO.getName()));
        }

        Tag saved = tagRepository.save(tag);
        return taxonomyMapper.toTagDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TagDTO> getAllTags() {
        return tagRepository.findAll().stream()
                .map(taxonomyMapper::toTagDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public TagDTO getTagBySlug(String slug) {
        Tag tag = tagRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Tag not found"));
        return taxonomyMapper.toTagDTO(tag);
    }

    @Override
    public void deleteTag(Integer tagId) {
        log.info("Deleting tag: {}", tagId);
        tagRepository.deleteById(tagId);
    }
}
