package com.liamo.workouts.auth.exception;

/**
 * Thrown when a user tries to register but the email is already in use.
 */
public class UserAlreadyExistsException extends InternalException {
    private final String email;

    public UserAlreadyExistsException(String email) {
        super("User already exists for email: " + email);
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String toString() {
        return "UserAlreadyExistsException{" +
            "email='" + email + '\'' +
            "} " + super.toString();
    }
}
