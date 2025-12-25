package com.liamo.workouts.auth.model.dto;

import com.liamo.workouts.auth.model.validation.PasswordsMatch;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * A DTO to hold information about a new LOCAL user registering.
 *
 * @param email           The user's email
 * @param password        The user's password
 * @param confirmPassword The user's password used to confirm it is the same
 * @param givenName       The user's first name
 * @param familyName      The user's last name
 */
@PasswordsMatch
public record CreateUserRequestDTO(
    @NotBlank
    @Email
    String email,

    @NotBlank
    String password,

    @NotBlank
    String confirmPassword,

    @NotBlank
    String givenName,

    @NotBlank
    String familyName
) {

}
