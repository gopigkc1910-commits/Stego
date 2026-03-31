package com.stego.backend.repository;

import com.stego.backend.entity.VerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface VerificationCodeRepository extends JpaRepository<VerificationCode, Long> {
    Optional<VerificationCode> findTopByEmailAndTypeOrderByCreatedAtDesc(String email, String type);
    void deleteByEmailAndType(String email, String type);
    long countByEmailAndCreatedAtAfter(String email, LocalDateTime since);
}
