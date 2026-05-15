package com.inkwell.post.repository;

import com.inkwell.post.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.List;

@Repository
public interface TagRepository extends JpaRepository<Tag, Integer> {
    Optional<Tag> findBySlug(String slug);
    Optional<Tag> findByName(String name);

    @Query("SELECT t FROM Tag t JOIN t.posts p GROUP BY t.tagId ORDER BY COUNT(p) DESC")
    List<Tag> findTrendingTags(Pageable pageable);
}
