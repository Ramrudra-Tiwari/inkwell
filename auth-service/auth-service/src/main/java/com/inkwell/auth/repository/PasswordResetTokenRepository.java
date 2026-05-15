package com.inkwell.auth.repository;

import com.inkwell.auth.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByEmailAndOtp(String email, String otp);
    Optional<PasswordResetToken> findByEmailAndOtpAndPurpose(String email, String otp, String purpose);
    void deleteByEmail(String email);
    void deleteByEmailAndPurpose(String email, String purpose);
    boolean existsByEmailAndPurpose(String email, String purpose);
}
