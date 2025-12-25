package com.liamo.workoutsauth.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserTest {

    @Test
    void testDefaultConstructor() {
        // When
        User user = new User();

        // Then
        assertThat(user.getId()).isNull();
        assertThat(user.getUsername()).isNull();
        assertThat(user.getPassword()).isNull();
        assertThat(user.isEnabled()).isTrue();
    }

    @Test
    void testParameterizedConstructor() {
        // When
        User user = new User("testuser", "password123");

        // Then
        assertThat(user.getUsername()).isEqualTo("testuser");
        assertThat(user.getPassword()).isEqualTo("password123");
        assertThat(user.isEnabled()).isTrue();
    }

    @Test
    void testSettersAndGetters() {
        // Given
        User user = new User();

        // When
        user.setId(1L);
        user.setUsername("testuser");
        user.setPassword("password123");
        user.setEnabled(false);

        // Then
        assertThat(user.getId()).isEqualTo(1L);
        assertThat(user.getUsername()).isEqualTo("testuser");
        assertThat(user.getPassword()).isEqualTo("password123");
        assertThat(user.isEnabled()).isFalse();
    }

    @Test
    void testEquals() {
        // Given
        User user1 = new User("testuser", "password123");
        user1.setId(1L);

        User user2 = new User("testuser", "password123");
        user2.setId(1L);

        User user3 = new User("otheruser", "password456");
        user3.setId(2L);

        // Then
        assertThat(user1).isEqualTo(user2);
        assertThat(user1).isNotEqualTo(user3);
        assertThat(user1).isNotEqualTo(null);
        assertThat(user1).isEqualTo(user1);
    }

    @Test
    void testHashCode() {
        // Given
        User user1 = new User("testuser", "password123");
        user1.setId(1L);

        User user2 = new User("testuser", "password123");
        user2.setId(1L);

        // Then
        assertThat(user1.hashCode()).isEqualTo(user2.hashCode());
    }

    @Test
    void testToString() {
        // Given
        User user = new User("testuser", "password123");
        user.setId(1L);

        // When
        String toString = user.toString();

        // Then
        assertThat(toString).contains("User");
        assertThat(toString).contains("id=1");
        assertThat(toString).contains("username='testuser'");
        assertThat(toString).contains("enabled=true");
        assertThat(toString).doesNotContain("password");
    }
}
