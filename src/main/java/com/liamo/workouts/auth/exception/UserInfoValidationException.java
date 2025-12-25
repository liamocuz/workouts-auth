package com.liamo.workouts.auth.exception;

import java.util.Map;

public class UserInfoValidationException extends PublicException {
    private final Map<String, String> fieldErrors;

    public UserInfoValidationException(Map<String, String> fieldErrors) {
        super(
            "Validation has failed for the user information.",
            "Please verify all fields are valid and non-blank."
        );
        this.fieldErrors = fieldErrors == null ? Map.of() : fieldErrors;
    }

    public Map<String, String> getFieldErrors() {
        return Map.copyOf(fieldErrors);
    }

    @Override
    public String toString() {
        return "ValidationException{" +
            "fieldErrors=" + fieldErrors +
            "} " + super.toString();
    }
}
