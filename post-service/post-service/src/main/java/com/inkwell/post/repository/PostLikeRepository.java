package com.inkwell.post.repository;

import com.inkwell.post.entity.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    boolean existsByPostIdAndUserId(Integer postId, Integer userId);

    Optional<PostLike> findByPostIdAndUserId(Integer postId, Integer userId);

    @Modifying
    @Transactional
    void deleteByPostId(Integer postId);
}
