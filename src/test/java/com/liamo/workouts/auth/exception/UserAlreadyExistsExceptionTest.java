package com.liamo.workouts.auth.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserAlreadyExistsExceptionTest {

    @Test
    void constructor_shouldStoreEmail() {
        // Arrange
        String email = "test@example.com";

        // Act
        UserAlreadyExistsException exception = new UserAlreadyExistsException(email);

        // Assert
        assertThat(exception.getEmail()).isEqualTo(email);
    }

    @Test
    void getMessage_shouldContainEmail() {
        // Arrange
        String email = "duplicate@example.com";

        // Act
        UserAlreadyExistsException exception = new UserAlreadyExistsException(email);

        // Assert
        assertThat(exception.getMessage()).contains(email);
        assertThat(exception.getMessage()).contains("User already exists");
    }

    @Test
    void exception_shouldBeInstanceOfInternalException() {
        // Arrange
        UserAlreadyExistsException exception = new UserAlreadyExistsException("test@example.com");

        // Assert
        assertThat(exception).isInstanceOf(InternalException.class);
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }

    @Test
    void toString_shouldIncludeEmailAndMessage() {
        // Arrange
        String email = "user@test.com";
        UserAlreadyExistsException exception = new UserAlreadyExistsException(email);

        // Act
        String result = exception.toString();

        // Assert
        assertThat(result).contains("UserAlreadyExistsException");
        assertThat(result).contains(email);
    }
}
