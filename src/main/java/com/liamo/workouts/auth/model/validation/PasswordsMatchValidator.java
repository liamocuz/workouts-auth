package com.liamo.workouts.auth.model.validation;

import com.liamo.workouts.auth.model.dto.CreateUserRequestDTO;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validator to ensure that the password and confirmPassword fields match
 * in the CreateUserRequestDTO.
 */
public class PasswordsMatchValidator implements ConstraintValidator<PasswordsMatch, CreateUserRequestDTO> {

    @Override
    public boolean isValid(CreateUserRequestDTO value, ConstraintValidatorContext context) {
        if (value.password() == null || value.confirmPassword() == null) {
            return false;
        }
        boolean matches = value.password().equals(value.confirmPassword());
        if (!matches) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                   .addPropertyNode("confirmPassword")
                   .addConstraintViolation();
        }
        return matches;
    }
}
