package com.inkwell.post.repository;

import com.inkwell.post.entity.PostView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface PostViewRepository extends JpaRepository<PostView, Long> {
    boolean existsByPostIdAndSessionId(Integer postId, String sessionId);

    @Modifying
    @Transactional
    void deleteByPostId(Integer postId);
}
