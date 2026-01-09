package com.liamo.workouts.auth.model.entity;

import com.liamo.workouts.auth.model.AuthProvider;
import com.liamo.workouts.auth.model.AuthRole;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserInfoTest {

    private static final String EMAIL = "test@example.com";
    private static final String SUB = "subject123";
    private static final AuthProvider PROVIDER = AuthProvider.LOCAL;
    private static final String PASSWORD = "pass123";
    private static final String GIVEN_NAME = "Bob";
    private static final String FAMILY_NAME = "Builder";

    private static UserInfo.Builder baseBuilder() {
        return UserInfo
            .newBuilder()
            .provider(PROVIDER)
            .sub(SUB)
            .email(EMAIL)
            .givenName(GIVEN_NAME)
            .familyName(FAMILY_NAME)
            .passwordHash(PASSWORD)
            .addRole(AuthRole.USER);
    }

    @Test
    void build_setAllFields() {
        UUID publicId = UUID.randomUUID();

        UserInfo user = UserInfo
            .newBuilder()
            .id(1L)
            .publicId(publicId)
            .email(EMAIL)
            .sub(SUB)
            .provider(PROVIDER)
            .givenName(GIVEN_NAME)
            .familyName(FAMILY_NAME)
            .passwordHash(PASSWORD)
            .addRole(AuthRole.ADMIN)
            .build();

        assertEquals(1L, user.getId());
        assertEquals(publicId, user.getPublicId());
        assertEquals(EMAIL, user.getEmail());
        assertEquals(SUB, user.getSub());
        assertEquals(PROVIDER, user.getProvider());
        assertEquals(GIVEN_NAME, user.getGivenName());
        assertEquals(FAMILY_NAME, user.getFamilyName());
        assertEquals(PASSWORD, user.getPasswordHash());

        assertFalse(user.isEmailVerified());
        assertTrue(user.isEnabled());

        assertTrue(user.getRoles().contains(AuthRole.ADMIN));

        // Display name defaults to given name if not set
        assertEquals(GIVEN_NAME, user.getDisplayName());

        // Some fields must be set by JPA, so expect to not be set upon build
        assertNull(user.getCreatedAt());
        assertNull(user.getUpdatedAt());
        assertNull(user.getLastLogin());

        // deletedAt should not be set on creation
        assertNull(user.getDeletedAt());
    }

    @Test
    void build_whenNonLocalProvider_thenPasswordBlank() {
        UserInfo user = baseBuilder()
            .provider(AuthProvider.GOOGLE)
            .build();

        assertEquals("", user.getPasswordHash());
    }

    @Test
    void build_whenNoSetPublicId_thenGenerateRandomUUID() {
        UserInfo user = baseBuilder().build();

        assertNotNull(user.getPublicId());
    }

    @Test
    void addRole_thenAddsNewRole() {
        UserInfo user = baseBuilder().build();

        user.addRole(AuthRole.ADMIN);

        assertTrue(user.getRoles().contains(AuthRole.USER));
        assertTrue(user.getRoles().contains(AuthRole.ADMIN));
        assertEquals(2, user.getRoles().size());
    }

    @Test
    void addRole_thenDoesNotAddDuplicateRole() {
        UserInfo user = baseBuilder().build();

        user.addRole(AuthRole.USER);

        assertTrue(user.getRoles().contains(AuthRole.USER));
        assertEquals(1, user.getRoles().size());
    }

    @Test
    void removeRole_thenRemovesRole() {
        UserInfo user = baseBuilder()
            .addRole(AuthRole.ADMIN)
            .build();

        user.removeRole(AuthRole.USER);

        assertFalse(user.getRoles().contains(AuthRole.USER));
        assertTrue(user.getRoles().contains(AuthRole.ADMIN));
        assertEquals(1, user.getRoles().size());
    }

    @Test
    void removeRole_thenAbsentRoleDoesNothing() {
        UserInfo user = baseBuilder().build();

        user.removeRole(AuthRole.ADMIN);

        assertTrue(user.getRoles().contains(AuthRole.USER));
        assertEquals(1, user.getRoles().size());
    }

    @Test
    void getRoles_thenIsUnmodifiable() {
        UserInfo user = baseBuilder().build();

        var unmodifiable = user.getRoles();
        assertThrows(UnsupportedOperationException.class, () -> unmodifiable.add(AuthRole.ADMIN));

        user.addRole(AuthRole.ADMIN);

        assertTrue(user.getRoles().contains(AuthRole.ADMIN));
        assertFalse(unmodifiable.contains(AuthRole.ADMIN));
    }

    @Test
    void equalsAndHashCode_whenSameSubAndProvider_thenAreEqual() {
        UserInfo user1 = UserInfo
            .newBuilder()
            .provider(PROVIDER)
            .sub(SUB)
            .email(EMAIL)
            .givenName(GIVEN_NAME)
            .familyName(FAMILY_NAME)
            .passwordHash(PASSWORD)
            .addRole(AuthRole.USER)
            .build();

        UserInfo user2 = UserInfo
            .newBuilder()
            .provider(PROVIDER)
            .sub(SUB)
            .email("other@example.com")
            .givenName("Other")
            .familyName("Name")
            .passwordHash("otherpass")
            .addRole(AuthRole.ADMIN)
            .build();

        assertEquals(user1, user2);
        assertEquals(user1.hashCode(), user2.hashCode());
    }

    @Test
    void equalsAndHashCode_whenDifferentSubOrProvider_thenNotEqual() {
        UserInfo user1 = baseBuilder().build();
        UserInfo user2 = baseBuilder().sub("different").build();
        UserInfo user3 = baseBuilder().provider(AuthProvider.GOOGLE).build();

        assertNotEquals(user1, user2);
        assertNotEquals(user1, user3);
        assertNotEquals(user2, user3);
    }

    @Test
    void settersAndGetters_thenUpdateAndReturnValues() {
        UserInfo user = baseBuilder().build();

        user.setEmail("new@example.com");
        user.setGivenName("Alice");
        user.setFamilyName("Smith");
        user.setDisplayName("Ali");
        user.setEmailVerified(true);
        user.setEnabled(false);
        Instant now = Instant.now();
        user.setLastLogin(now);

        assertEquals("new@example.com", user.getEmail());
        assertEquals("Alice", user.getGivenName());
        assertEquals("Smith", user.getFamilyName());
        assertEquals("Ali", user.getDisplayName());
        assertTrue(user.isEmailVerified());
        assertFalse(user.isEnabled());
        assertEquals(now, user.getLastLogin());
    }

}