package com.inkwell.auth.repository;

import com.inkwell.auth.entity.Follow;
import com.inkwell.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Integer> {
    Optional<Follow> findByFollowerAndFollowed(User follower, User followed);
    boolean existsByFollowerAndFollowed(User follower, User followed);
    
    List<Follow> findByFollowed(User followed);
    List<Follow> findByFollower(User follower);
    
    long countByFollowed(User followed);
    long countByFollower(User follower);
}
