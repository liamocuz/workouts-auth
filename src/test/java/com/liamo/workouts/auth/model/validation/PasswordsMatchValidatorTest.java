package com.liamo.workouts.auth.model.validation;

import com.liamo.workouts.auth.model.dto.CreateUserRequestDTO;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class PasswordsMatchValidatorTest {

    private final PasswordsMatchValidator validator = new PasswordsMatchValidator();
    private final ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);

    @BeforeEach
    void beforeEach() {
        doNothing().when(context).disableDefaultConstraintViolation();
        when(context.getDefaultConstraintMessageTemplate()).thenReturn("");

        ConstraintValidatorContext.ConstraintViolationBuilder builder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
        when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(builder);

        ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext nodeContext = mock(ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext.class);
        when(builder.addPropertyNode(anyString())).thenReturn(nodeContext);
        when(nodeContext.addConstraintViolation()).thenReturn(context);
    }

    @Test
    void isValid_whenPasswordsMatch_thenReturnTrue() {

        CreateUserRequestDTO dto = new CreateUserRequestDTO(
            "email@example.com",
            "password123",
            "password123",
            "John",
            "Doe"
        );

        assertTrue(validator.isValid(dto, context));
    }


    @Test
    void isValid_whenPasswordsDoNotMatch_thenReturnFalse() {
        CreateUserRequestDTO dto = new CreateUserRequestDTO(
            "email@example.com",
            "password123",
            "differentPassword",
            "John",
            "Doe"
        );

        assertFalse(validator.isValid(dto, context));
    }

    @Test
    void isValid_whenPasswordIsNull_thenReturnFalse() {
        CreateUserRequestDTO dto = new CreateUserRequestDTO(
            "email@exmaple.com",
            null,
            "password123",
            "John",
            "Doe"
        );

        assertFalse(validator.isValid(dto, context));
    }
}