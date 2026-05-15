package com.inkwell.category.service;

import com.inkwell.category.dto.TagDTO;
import com.inkwell.category.dto.TaxonomyRequest;
import com.inkwell.category.entity.Tag;
import com.inkwell.category.exception.TaxonomyNotFoundException;
import com.inkwell.category.mapper.TagMapper;
import com.inkwell.category.repository.TagRepository;
import com.inkwell.category.util.SlugUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of TagService.
 * Manages tag creation, updates, deletion, and post associations.
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class TagServiceImpl implements TagService {

    private final TagRepository tagRepository;
    private final TagMapper tagMapper;

    @Override
    public TagDTO createTag(TaxonomyRequest request) {
        log.debug("Creating tag: {}", request.getName());

        // Generate slug from name
        String slug = SlugUtil.generateSlug(request.getName());

        // Check if slug already exists
        if (tagRepository.existsBySlug(slug)) {
            throw new IllegalArgumentException("Tag slug already exists: " + slug);
        }

        // Create and save tag
        Tag tag = Tag.builder()
                .name(request.getName())
                .slug(slug)
                .postCount(0)
                .build();

        tag = tagRepository.save(tag);
        log.info("Tag created successfully: {} (ID: {})", slug, tag.getTagId());

        return tagMapper.toDTO(tag);
    }

    @Override
    @Transactional(readOnly = true)
    public TagDTO getTagById(Integer tagId) {
        log.debug("Fetching tag by ID: {}", tagId);

        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> TaxonomyNotFoundException.tagNotFound(tagId));

        return tagMapper.toDTO(tag);
    }

    @Override
    @Transactional(readOnly = true)
    public TagDTO getTagBySlug(String slug) {
        log.debug("Fetching tag by slug: {}", slug);

        Tag tag = tagRepository.findBySlug(slug)
                .orElseThrow(() -> TaxonomyNotFoundException.tagNotFound(slug));

        return tagMapper.toDTO(tag);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TagDTO> getAllTags() {
        log.debug("Fetching all tags");

        return tagRepository.findAll()
                .stream()
                .map(tagMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TagDTO> getTrendingTags(int limit) {
        log.debug("Fetching top {} trending tags", limit);

        return tagRepository.findTopTrendingTags(limit)
                .stream()
                .map(tagMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public TagDTO updateTag(Integer tagId, TaxonomyRequest request) {
        log.debug("Updating tag: {}", tagId);

        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> TaxonomyNotFoundException.tagNotFound(tagId));

        // Update name and slug if changed
        if (!tag.getName().equals(request.getName())) {
            String newSlug = SlugUtil.generateSlug(request.getName());
            if (!tag.getSlug().equals(newSlug) && tagRepository.existsBySlug(newSlug)) {
                throw new IllegalArgumentException("Tag slug already exists: " + newSlug);
            }
            tag.setName(request.getName());
            tag.setSlug(newSlug);
        }

        tag = tagRepository.save(tag);
        log.info("Tag updated successfully: {}", tagId);

        return tagMapper.toDTO(tag);
    }

    @Override
    public void deleteTag(Integer tagId) {
        log.debug("Deleting tag: {}", tagId);

        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> TaxonomyNotFoundException.tagNotFound(tagId));

        tagRepository.delete(tag);
        log.info("Tag deleted successfully: {}", tagId);
    }

    @Override
    public void assignPostToTag(Integer tagId, Integer postId) {
        log.debug("Assigning post {} to tag {}", postId, tagId);

        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> TaxonomyNotFoundException.tagNotFound(tagId));

        tagRepository.incrementPostCount(tagId);
        log.info("Post {} assigned to tag {}", postId, tagId);
    }

    @Override
    public void removePostFromTag(Integer tagId, Integer postId) {
        log.debug("Removing post {} from tag {}", postId, tagId);

        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> TaxonomyNotFoundException.tagNotFound(tagId));

        tagRepository.decrementPostCount(tagId);
        log.info("Post {} removed from tag {}", postId, tagId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TagDTO> searchByName(String namePattern) {
        log.debug("Searching tags by name pattern: {}", namePattern);

        return tagRepository.searchByName(namePattern)
                .stream()
                .map(tagMapper::toDTO)
                .collect(Collectors.toList());
    }
}

