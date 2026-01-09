package com.liamo.workouts.auth.model.validation;

import com.liamo.workouts.auth.model.entity.UserInfo;
import com.liamo.workouts.auth.exception.UserInfoValidationException;
import com.liamo.workouts.auth.model.AuthProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Validates the user inputted fields of a {@link UserInfo.Builder object}
 */
public class UserInfoBuilderValidator {
    private static final Logger logger = LoggerFactory.getLogger(UserInfoBuilderValidator.class);

    private static final String EMPTY_PASSWORD = "";

    public static void validate(UserInfo.Builder builder) {
        Map<String, String> fieldErrors = new HashMap<>();

        if (builder.getProvider() == null) {
            fieldErrors.put("provider", "Provider is required");
        }

        if (builder.getSub() == null || builder.getSub().isBlank()) {
            fieldErrors.put("sub", "Subject (sub) is required");
        }
        if (builder.getEmail() == null || builder.getEmail().isBlank()) {
            fieldErrors.put("email", "Email is required");
        }
        if (builder.getGivenName() == null || builder.getGivenName().isBlank()) {
            fieldErrors.put("firstName", "First name is required");
        }
        if (builder.getFamilyName() == null || builder.getFamilyName().isBlank()) {
            fieldErrors.put("lastName", "Last name is required");
        }

        // Password is truly required for LOCAL provider, but can be empty for others
        // Set to empty string again just in case for non-LOCAL provider
        if (builder.getProvider() == AuthProvider.LOCAL) {
            if (builder.getPasswordHash() == null || builder.getPasswordHash().isBlank()) {
                fieldErrors.put("password", "Password is required");
            }
        } else {
            builder.passwordHash(EMPTY_PASSWORD);
        }

        if (builder.getRoles().isEmpty()) {
            fieldErrors.put("roles", "At least one role is required");
        }

        if (!fieldErrors.isEmpty()) {
            logger.debug("UserInfo.build() errors: {}", fieldErrors);
            logger.debug("UserInfo.Builder state: {}", builder);

            throw new UserInfoValidationException(fieldErrors);
        }
    }
}
