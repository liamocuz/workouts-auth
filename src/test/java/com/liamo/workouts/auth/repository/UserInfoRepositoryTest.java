package com.liamo.workouts.auth.repository;

import com.liamo.workouts.auth.PostgreSQLTestcontainer;
import com.liamo.workouts.auth.model.AuthProvider;
import com.liamo.workouts.auth.model.AuthRole;
import com.liamo.workouts.auth.model.entity.UserInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import(PostgreSQLTestcontainer.class)
class UserInfoRepositoryTest {

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

    private static UserInfo createOidcUser() {
        return UserInfo
            .newBuilder()
            .provider(AuthProvider.GOOGLE)
            .sub(UUID.randomUUID().toString())  // Google's sub is really a long number string
            .email("bob@gmail.com")
            .givenName("Bob")
            .familyName("Builder")
            .addRole(AuthRole.USER)
            .build();
    }

    private UserInfo localUser;

    @Autowired
    private UserInfoRepository userInfoRepository;

    @BeforeEach
    void beforeEach() {
        localUser = userInfoRepository.save(createLocalUser());
    }

    @Test
    void save_whenValidUser_thenPersistWithJPAFields() {
        UserInfo saved = userInfoRepository.save(createOidcUser());

        // Test that JPA annotations work and all correct data is set
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getId()).isGreaterThan(0);
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
        assertThat(saved.getLastLogin()).isNull();  // This should not be updated upon save
        assertThat(saved.getDeletedAt()).isNull();  // This should be null upon creation
    }

    @Test
    void findById_whenUserExists_thenReturnUser() {
        Optional<UserInfo> found = userInfoRepository.findById(localUser.getId());

        assertThat(found.isPresent()).isTrue();
        UserInfo user = found.get();
        assertThat(user.getId()).isEqualTo(localUser.getId());
        assertThat(user.getEmail()).isEqualTo(localUser.getEmail());
        assertThat(user.getProvider()).isEqualTo(localUser.getProvider());
        assertThat(user.getSub()).isEqualTo(localUser.getSub());

        assertThat(user.getCreatedAt()).isEqualTo(localUser.getCreatedAt());
        assertThat(user.getUpdatedAt()).isEqualTo(localUser.getUpdatedAt());

        assertThat(user.getLastLogin()).isNull();
        assertThat(user.getLastLogin()).isEqualTo(localUser.getLastLogin()); // Should be NULL

        assertThat(user.getDeletedAt()).isNull();
        assertThat(user.getDeletedAt()).isEqualTo(localUser.getDeletedAt()); // Should be NULL

        assertThat(user.isEmailVerified()).isEqualTo(localUser.isEmailVerified());
        assertThat(user.isEnabled()).isEqualTo(localUser.isEnabled());

        assertThat(user.getGivenName()).isEqualTo(localUser.getGivenName());
        assertThat(user.getFamilyName()).isEqualTo(localUser.getFamilyName());
        assertThat(user.getPasswordHash()).isEqualTo(localUser.getPasswordHash());

        assertThat(user.getRoles()).isEqualTo(localUser.getRoles());
    }

    @Test
    void findById_whenUserDoesNotExist_thenReturnNull() {
        Optional<UserInfo> found = userInfoRepository.findById(1000L);
        assertThat(found).isEmpty();
    }

    @Test
    void findById_whenDeleteUser_thenReturnUser() {
        userInfoRepository.deleteById(localUser.getId());
        Optional<UserInfo> found = userInfoRepository.findById(localUser.getId());
        assertThat(found).isEmpty();
    }

    @Test
    void save_whenUpdatedLastLogin_thenPersistWithUpdatedLastLogin() {
        Instant now = Instant.now();
        localUser.setLastLogin(now);
        userInfoRepository.save(localUser);

        Optional<UserInfo> found = userInfoRepository.findById(localUser.getId());
        assertThat(found.isPresent()).isTrue();
        assertThat(found.get().getLastLogin()).isEqualTo(now);
    }

    @Test
    void save_whenDuplicateProviderAndSub_thenThrowException() {
        UserInfo duplicate = UserInfo
            .newBuilder()
            .sub(localUser.getSub())
            .provider(AuthProvider.LOCAL)
            .email("duplicate@aol.com")
            .givenName("duplicate")
            .familyName("duplicate")
            .passwordHash("duplicate")
            .addRole(AuthRole.USER)
            .build();

        // There is a unique index on provider and sub
        assertThatThrownBy(() -> userInfoRepository.save(duplicate))
            .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void save_duplicateProviderAndEmail_throwException() {
        UserInfo duplicate = UserInfo
            .newBuilder()
            .sub(UUID.randomUUID().toString())
            .provider(AuthProvider.LOCAL)
            .email(localUser.getEmail())
            .givenName("duplicate")
            .familyName("duplicate")
            .passwordHash("duplicate")
            .addRole(AuthRole.USER)
            .build();

        // There is a unique index on provider and email
        assertThatThrownBy(() -> userInfoRepository.save(duplicate))
            .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void save_whenMultipleRoles_thenPersistAllRoles() {
        UserInfo oidc = createOidcUser();
        oidc.addRole(AuthRole.ADMIN);
        userInfoRepository.save(oidc);

        Optional<UserInfo> found = userInfoRepository.findById(oidc.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getRoles()).containsExactlyInAnyOrder(AuthRole.ADMIN, AuthRole.USER);
    }

    @Test
    void findByProviderAndSub_whenExists_thenReturnUser() {
        Optional<UserInfo> foundUser = userInfoRepository.findByProviderAndSub(localUser.getProvider(), localUser.getSub());
        assertThat(foundUser).isPresent();
    }

    @Test
    void findByProviderAndSub_whenNotExists_thenReturnEmptyOptional() {
        Optional<UserInfo> foundUser = userInfoRepository.findByProviderAndSub(AuthProvider.GOOGLE, localUser.getSub());
        assertThat(foundUser).isEmpty();
    }

    @Test
    void existsByProviderAndSub_whenExists_thenReturnTrue() {
        boolean exists = userInfoRepository.existsByProviderAndSub(localUser.getProvider(), localUser.getSub());
        assertThat(exists).isTrue();
    }

    @Test
    void existsByProviderAndSub_whenNotExists_thenReturnFalse() {
        boolean exists = userInfoRepository.existsByProviderAndSub(AuthProvider.GOOGLE, localUser.getSub());
        assertThat(exists).isFalse();
    }

    @Test
    void findByProviderAndEmail_whenExists_thenReturnUser() {
        Optional<UserInfo> foundUser = userInfoRepository.findByProviderAndEmailIgnoreCase(localUser.getProvider(), localUser.getEmail());
        assertThat(foundUser).isPresent();
    }

    @Test
    void findByProviderAndEmail_whenNotExists_thenReturnEmptyOptional() {
        Optional<UserInfo> foundUser = userInfoRepository.findByProviderAndEmailIgnoreCase(AuthProvider.GOOGLE, localUser.getEmail());
        assertThat(foundUser).isEmpty();
    }

    @Test
    void findByProviderAndEmail_whenDifferentCase_thenReturnUser() {
        String emailUpperCase = localUser.getEmail().toUpperCase();
        Optional<UserInfo> foundUser = userInfoRepository.findByProviderAndEmailIgnoreCase(localUser.getProvider(), emailUpperCase);
        assertThat(foundUser).isPresent();
    }

    @Test
    void existsByProviderAndEmail_whenExists_thenReturnTrue() {
        boolean exists = userInfoRepository.existsByProviderAndEmailIgnoreCase(localUser.getProvider(), localUser.getEmail());
        assertThat(exists).isTrue();
    }

    @Test
    void existsByProviderAndEmail_whenNotExists_thenReturnFalse() {
        boolean exists = userInfoRepository.existsByProviderAndEmailIgnoreCase(AuthProvider.GOOGLE, localUser.getEmail());
        assertThat(exists).isFalse();
    }

    @Test
    void existsByProviderAndEmail_whenDifferentCase_thenReturnTrue() {
        String emailUpperCase = localUser.getEmail().toUpperCase();
        boolean exists = userInfoRepository.existsByProviderAndEmailIgnoreCase(localUser.getProvider(), emailUpperCase);
        assertThat(exists).isTrue();
    }

    @Test
    void findByPublicId_whenExists_thenReturnUser() {
        Optional<UserInfo> foundUser = userInfoRepository.findByPublicId(localUser.getPublicId());
        assertThat(foundUser).isPresent();
    }

    @Test
    void findByPublicId_whenNotExists_thenReturnEmptyOptional() {
        Optional<UserInfo> foundUser = userInfoRepository.findByPublicId(UUID.randomUUID());
        assertThat(foundUser).isEmpty();
    }
}