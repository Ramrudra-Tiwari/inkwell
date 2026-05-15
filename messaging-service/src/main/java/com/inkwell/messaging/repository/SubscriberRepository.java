package com.inkwell.messaging.repository;

import com.inkwell.messaging.entity.Subscriber;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Subscriber entity operations.
 */
@Repository
public interface SubscriberRepository extends JpaRepository<Subscriber, Integer> {

    /**
     * Find subscriber by email.
     */
    Optional<Subscriber> findByEmail(String email);

    /**
     * Find subscriber by token.
     */
    Optional<Subscriber> findByToken(String token);

    /**
     * Find all active subscribers.
     */
    @Query("SELECT s FROM Subscriber s WHERE s.status = 'ACTIVE'")
    List<Subscriber> findAllActiveSubscribers();

    /**
     * Find subscribers by user ID.
     */
    List<Subscriber> findByUserId(Integer userId);

    /**
     * Update subscriber status by token.
     */
    @Modifying
    @Query("UPDATE Subscriber s SET s.status = :status WHERE s.token = :token")
    int updateStatusByToken(@Param("token") String token, @Param("status") Subscriber.SubscriberStatus status);

    /**
     * Check if email is already subscribed (active or pending).
     */
    @Query("SELECT COUNT(s) > 0 FROM Subscriber s WHERE s.email = :email AND s.status IN ('ACTIVE', 'PENDING')")
    boolean existsByEmailAndActiveOrPending(@Param("email") String email);
}
