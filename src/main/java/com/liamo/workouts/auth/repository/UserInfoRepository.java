package com.liamo.workouts.auth.repository;

import com.liamo.workouts.auth.model.AuthProvider;
import com.liamo.workouts.auth.model.entity.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for managing {@link UserInfo} entities.
 */
public interface UserInfoRepository extends JpaRepository<UserInfo, Long> {
    Optional<UserInfo> findByProviderAndSub(AuthProvider provider, String sub);

    boolean existsByProviderAndSub(AuthProvider provider, String sub);

    Optional<UserInfo> findByProviderAndEmailIgnoreCase(AuthProvider provider, String email);

    /**
     * This should only be used for fetch {@link AuthProvider#LOCAL} provider and email.
     */
    boolean existsByProviderAndEmailIgnoreCase(AuthProvider provider, String email);

    Optional<UserInfo> findByPublicId(UUID publicId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update UserInfo u set u.lastLogin = CURRENT_TIMESTAMP where u.publicId = :publicId")
    int updateLastLoginByPublicId(@Param("publicId") UUID publicId);
}