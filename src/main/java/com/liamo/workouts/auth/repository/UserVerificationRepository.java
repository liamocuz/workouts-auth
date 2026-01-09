package com.liamo.workouts.auth.repository;

import com.liamo.workouts.auth.model.entity.UserVerification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for managing UserVerification entities.
 */
public interface UserVerificationRepository extends JpaRepository<UserVerification, Long> {
    Optional<UserVerification> findByToken(UUID token);
}
