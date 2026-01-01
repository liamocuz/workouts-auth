package com.liamo.workouts.auth.exception;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class UserInfoValidationExceptionTest {

    @Test
    void constructor_shouldStoreFieldErrors() {
        // Arrange
        Map<String, String> fieldErrors = Map.of(
            "email", "Email is required",
            "password", "Password must be at least 8 characters"
        );

        // Act
        UserInfoValidationException exception = new UserInfoValidationException(fieldErrors);

        // Assert
        assertThat(exception.getFieldErrors()).isEqualTo(fieldErrors);
    }

    @Test
    void constructor_withNullFieldErrors_shouldCreateEmptyMap() {
        // Act
        UserInfoValidationException exception = new UserInfoValidationException(null);

        // Assert
        assertThat(exception.getFieldErrors()).isEmpty();
        assertThat(exception.getFieldErrors()).isNotNull();
    }

    @Test
    void getFieldErrors_shouldReturnImmutableCopy() {
        // Arrange
        Map<String, String> fieldErrors = Map.of("field", "error");
        UserInfoValidationException exception = new UserInfoValidationException(fieldErrors);

        // Act
        Map<String, String> retrieved = exception.getFieldErrors();

        // Assert - Attempting to modify should throw exception
        assertThat(retrieved).isEqualTo(fieldErrors);
        assertThat(retrieved).isUnmodifiable();
    }

    @Test
    void getMessage_shouldReturnStandardMessage() {
        // Arrange
        UserInfoValidationException exception = new UserInfoValidationException(Map.of());

        // Act
        String message = exception.getMessage();

        // Assert
        assertThat(message).isEqualTo("Validation has failed for the user information.");
    }

    @Test
    void getAction_shouldReturnHelpfulAction() {
        // Arrange
        UserInfoValidationException exception = new UserInfoValidationException(Map.of());

        // Act
        String action = exception.getAction();

        // Assert
        assertThat(action).contains("verify all fields");
        assertThat(action).contains("valid and non-blank");
    }

    @Test
    void exception_shouldBeInstanceOfPublicException() {
        // Arrange
        UserInfoValidationException exception = new UserInfoValidationException(Map.of());

        // Assert
        assertThat(exception).isInstanceOf(PublicException.class);
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }

    @Test
    void toString_shouldIncludeFieldErrors() {
        // Arrange
        Map<String, String> fieldErrors = Map.of("username", "Username is taken");
        UserInfoValidationException exception = new UserInfoValidationException(fieldErrors);

        // Act
        String result = exception.toString();

        // Assert
        assertThat(result).contains("ValidationException");
        assertThat(result).contains("fieldErrors");
    }
}
