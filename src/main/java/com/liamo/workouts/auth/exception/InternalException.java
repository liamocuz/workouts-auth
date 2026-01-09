package com.liamo.workouts.auth.exception;

/**
 * Represents an exception that cannot be returned to a user.
 * All related data can be logged but not shown to the user.
 */
public abstract class InternalException extends RuntimeException {
    protected InternalException(String message) {
        super(message);
    }
}
