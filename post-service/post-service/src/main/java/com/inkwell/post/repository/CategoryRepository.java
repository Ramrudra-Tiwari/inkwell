package com.inkwell.post.repository;

import com.inkwell.post.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {
    Optional<Category> findBySlug(String slug);
    
    @Query("SELECT c FROM Category c WHERE c.parentCategory IS NULL")
    List<Category> findRootCategories();
    
    List<Category> findByParentCategory_CategoryId(Integer parentId);
}
