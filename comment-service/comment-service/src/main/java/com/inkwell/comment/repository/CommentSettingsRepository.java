package com.inkwell.comment.repository;

import com.inkwell.comment.entity.CommentSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentSettingsRepository extends JpaRepository<CommentSettings, Integer> {
}
