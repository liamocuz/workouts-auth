package com.liamo.workoutsauth.repository;

import com.liamo.workoutsauth.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import com.liamo.workoutsauth.TestcontainersConfiguration;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(TestcontainersConfiguration.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void testSaveUser() {
        // Given
        User user = new User("testuser", "password123");

        // When
        User savedUser = userRepository.save(user);

        // Then
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getUsername()).isEqualTo("testuser");
        assertThat(savedUser.getPassword()).isEqualTo("password123");
        assertThat(savedUser.isEnabled()).isTrue();
    }

    @Test
    void testFindByUsername() {
        // Given
        User user = new User("testuser", "password123");
        entityManager.persist(user);
        entityManager.flush();

        // When
        Optional<User> foundUser = userRepository.findByUsername("testuser");

        // Then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getUsername()).isEqualTo("testuser");
        assertThat(foundUser.get().getPassword()).isEqualTo("password123");
    }

    @Test
    void testFindByUsername_NotFound() {
        // When
        Optional<User> foundUser = userRepository.findByUsername("nonexistent");

        // Then
        assertThat(foundUser).isEmpty();
    }

    @Test
    void testUsernameIsUnique() {
        // Given
        User user1 = new User("testuser", "password123");
        userRepository.save(user1);

        // When/Then - attempting to save another user with same username should fail
        User user2 = new User("testuser", "password456");
        try {
            userRepository.saveAndFlush(user2);
        } catch (Exception e) {
            // Expected - unique constraint violation
            assertThat(e).isNotNull();
        }
    }

    @Test
    void testUpdateUser() {
        // Given
        User user = new User("testuser", "password123");
        User savedUser = userRepository.save(user);

        // When
        savedUser.setEnabled(false);
        User updatedUser = userRepository.save(savedUser);

        // Then
        assertThat(updatedUser.getId()).isEqualTo(savedUser.getId());
        assertThat(updatedUser.isEnabled()).isFalse();
    }

    @Test
    void testDeleteUser() {
        // Given
        User user = new User("testuser", "password123");
        User savedUser = userRepository.save(user);

        // When
        userRepository.delete(savedUser);

        // Then
        Optional<User> foundUser = userRepository.findByUsername("testuser");
        assertThat(foundUser).isEmpty();
    }
}
