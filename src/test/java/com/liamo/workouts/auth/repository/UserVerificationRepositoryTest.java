package com.liamo.workouts.auth.repository;

import com.liamo.workouts.auth.PostgreSQLTestcontainer;
import com.liamo.workouts.auth.model.AuthProvider;
import com.liamo.workouts.auth.model.AuthRole;
import com.liamo.workouts.auth.model.entity.UserInfo;
import com.liamo.workouts.auth.model.entity.UserVerification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


@DataJpaTest
@Import(PostgreSQLTestcontainer.class)
class UserVerificationRepositoryTest {

    private static UserInfo createLocalUser() {
        return UserInfo
            .newBuilder()
            .provider(AuthProvider.LOCAL)
            .sub(UUID.randomUUID().toString())
            .email("bob@aol.com")
            .givenName("Bob")
            .familyName("Builder")
            .passwordHash("password")
            .addRole(AuthRole.ADMIN)
            .build();
    }

    @Autowired
    private UserVerificationRepository userVerificationRepository;

    @Autowired
    private UserInfoRepository userInfoRepository;

    private UserVerification userVerification;

    @BeforeEach
    void beforeEach() {
        UserInfo localUser = userInfoRepository.save(createLocalUser());
        userVerification = userVerificationRepository.save(new UserVerification(localUser, 24));

    }

    @Test
    void findByToken_whenExists_thenReturnUserVerification() {
        Optional<UserVerification> found = userVerificationRepository.findByToken(userVerification.getToken());
        assertThat(found).isPresent();
    }

    @Test
    void findByToken_whenNotExists_thenReturnNull() {
        Optional<UserVerification> found = userVerificationRepository.findByToken(UUID.randomUUID());
        assertThat(found).isEmpty();
    }
}