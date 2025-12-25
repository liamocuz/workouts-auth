package com.liamo.workouts.auth.model.validation;

import com.liamo.workouts.auth.model.entity.UserInfo;
import com.liamo.workouts.auth.exception.UserInfoValidationException;
import com.liamo.workouts.auth.model.AuthRole;
import com.liamo.workouts.auth.model.AuthProvider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserInfoBuilderValidatorTest {
    private static final String SUB = "1234abcd";
    private static final String EMAIL = "bob@gmail.com";
    private static final String GIVEN_NAME = "Bob";
    private static final String FAMILY_NAME = "Smith";
    private static final String PASSWORD = "password";

    @Test
    void validate_whenLocalProviderAndAllFieldsValid_thenDoNotThrow() {
        UserInfo.Builder builder = UserInfo
            .newBuilder()
            .sub(SUB)
            .email(EMAIL)
            .provider(AuthProvider.LOCAL)
            .givenName(GIVEN_NAME)
            .familyName(FAMILY_NAME)
            .passwordHash(PASSWORD)
            .addRole(AuthRole.USER);

        assertDoesNotThrow(() -> UserInfoBuilderValidator.validate(builder));
    }

    @Test
    void validate_whenNonLocalProviderAndAllFieldsValid_thenDoNotThrow() {
        UserInfo.Builder builder = UserInfo
            .newBuilder()
            .sub(SUB)
            .email(EMAIL)
            .provider(AuthProvider.GOOGLE)
            .givenName(GIVEN_NAME)
            .familyName(FAMILY_NAME)
            .addRole(AuthRole.USER);

        assertDoesNotThrow(() -> UserInfoBuilderValidator.validate(builder));
    }

    @Test
    void validate_whenMissingEmailAndNameInfo_thenThrow() {
        UserInfo.Builder builder = UserInfo
            .newBuilder()
            .provider(AuthProvider.LOCAL)
            .sub(SUB)
            .passwordHash(PASSWORD)
            .addRole(AuthRole.USER);

        UserInfoValidationException ex =
            assertThrows(UserInfoValidationException.class, () -> UserInfoBuilderValidator.validate(builder));
        assertEquals(3, ex.getFieldErrors().size());
        assertTrue(ex.getFieldErrors().containsKey("email"));
        assertTrue(ex.getFieldErrors().containsKey("firstName"));
        assertTrue(ex.getFieldErrors().containsKey("lastName"));
    }

    @Test
    void validate_whenLocalProviderAndNoPassword_thenThrow() {
        UserInfo.Builder builder = UserInfo
            .newBuilder()
            .sub(SUB)
            .email(EMAIL)
            .provider(AuthProvider.LOCAL)
            .givenName(GIVEN_NAME)
            .familyName(FAMILY_NAME)
            .addRole(AuthRole.USER);

        UserInfoValidationException ex =
            assertThrows(UserInfoValidationException.class, () -> UserInfoBuilderValidator.validate(builder));
        assertEquals(1, ex.getFieldErrors().size());
        assertTrue(ex.getFieldErrors().containsKey("password"));
    }

    @Test
    void validate_whenMissingRole_thenThrow() {
        UserInfo.Builder builder = UserInfo
            .newBuilder()
            .provider(AuthProvider.GOOGLE)
            .sub(SUB)
            .email(EMAIL)
            .givenName(GIVEN_NAME)
            .familyName(FAMILY_NAME);


        UserInfoValidationException ex =
            assertThrows(UserInfoValidationException.class, () -> UserInfoBuilderValidator.validate(builder));
        assertEquals(1, ex.getFieldErrors().size());
        assertTrue(ex.getFieldErrors().containsKey("roles"));
    }

    @Test
    void validate_whenInvalidEmail_thenThrow() {
        UserInfo.Builder builder = UserInfo
            .newBuilder()
            .provider(AuthProvider.GOOGLE)
            .sub(SUB)
            .email(" ")
            .givenName(GIVEN_NAME)
            .familyName(FAMILY_NAME)
            .addRole(AuthRole.USER);

        UserInfoValidationException ex =
            assertThrows(UserInfoValidationException.class, () -> UserInfoBuilderValidator.validate(builder));
        assertEquals(1, ex.getFieldErrors().size());
        assertTrue(ex.getFieldErrors().containsKey("email"));
    }

    @Test
    void validate_whenMissingSub_thenThrow() {
        UserInfo.Builder builder = UserInfo
            .newBuilder()
            .email(EMAIL)
            .givenName(GIVEN_NAME)
            .familyName(FAMILY_NAME)
            .provider(AuthProvider.GOOGLE)
            .addRole(AuthRole.USER);

        UserInfoValidationException ex =
            assertThrows(UserInfoValidationException.class, () -> UserInfoBuilderValidator.validate(builder));
        assertEquals(1, ex.getFieldErrors().size());
        assertTrue(ex.getFieldErrors().containsKey("sub"));
    }

    @Test
    void validate_whenMissingProvider_thenThrow() {
        UserInfo.Builder builder = UserInfo
            .newBuilder()
            .sub(SUB)
            .email(EMAIL)
            .givenName(GIVEN_NAME)
            .familyName(FAMILY_NAME)
            .addRole(AuthRole.USER);

        UserInfoValidationException ex =
            assertThrows(UserInfoValidationException.class, () -> UserInfoBuilderValidator.validate(builder));
        assertEquals(1, ex.getFieldErrors().size());
        assertTrue(ex.getFieldErrors().containsKey("provider"));
    }

    @Test
    void validate_whenMissingAllFields_thenThrowWithAllErrors() {
        UserInfo.Builder builder = UserInfo
            .newBuilder();

        UserInfoValidationException ex =
            assertThrows(UserInfoValidationException.class, () -> UserInfoBuilderValidator.validate(builder));
        assertEquals(6, ex.getFieldErrors().size());
        assertTrue(ex.getFieldErrors().containsKey("provider"));
        assertTrue(ex.getFieldErrors().containsKey("sub"));
        assertTrue(ex.getFieldErrors().containsKey("email"));
        assertTrue(ex.getFieldErrors().containsKey("firstName"));
        assertTrue(ex.getFieldErrors().containsKey("lastName"));
        assertTrue(ex.getFieldErrors().containsKey("roles"));
    }
}