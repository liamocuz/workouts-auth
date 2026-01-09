package com.liamo.workouts.auth.exception;

/**
 * Represents an exception that can be returned to the user.
 */
public abstract class PublicException extends RuntimeException {
    /**
     * A user-friendly message that describes the issue.
     */
    private final String message;

    /**
     * An action the user can take to resolve the issue.
     */
    private final String action;

    public PublicException(String message, String action) {
        super();
        this.message = message;
        this.action = action;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public String getAction() {
        return action;
    }

    @Override
    public String toString() {
        return "PublicException{" +
            "message='" + message + '\'' +
            ", action='" + action + '\'' +
            '}';
    }
}
