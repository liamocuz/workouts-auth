package com.liamo.workouts.auth.service;

import com.liamo.workouts.auth.exception.UserAlreadyExistsException;
import com.liamo.workouts.auth.model.AuthProvider;
import com.liamo.workouts.auth.model.AuthRole;
import com.liamo.workouts.auth.model.entity.UserInfo;
import com.liamo.workouts.auth.model.entity.UserVerification;
import com.liamo.workouts.auth.model.dto.CreateUserRequestDTO;
import com.liamo.workouts.auth.repository.UserInfoRepository;
import com.liamo.workouts.auth.repository.UserVerificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserInfoRepository userInfoRepository;
    @Mock
    private UserVerificationRepository userVerificationRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserInfoService userInfoService;

    private final String EMAIL = "user@example.com";
    private static final String GIVEN_NAME = "Bob";
    private static final String FAMILY_NAME = "Builder";
    private final String PASSWORD = "password";
    private final String ENCODED_PASSWORD = "encodedPassword";

    private final CreateUserRequestDTO CREATE_USER_REQUEST = new CreateUserRequestDTO(
        EMAIL,
        PASSWORD,
        PASSWORD,
        "Bob",
        "Builder"
    );

    @Test
    void registerNewLocalUser_userDoesNotExist_savesUserAndReturnsVerification() {
        // Arrange
        when(userInfoRepository.existsByProviderAndEmailIgnoreCase(AuthProvider.LOCAL, EMAIL)).thenReturn(false);
        when(passwordEncoder.encode(PASSWORD)).thenReturn(ENCODED_PASSWORD);

        UserInfo savedUser = UserInfo
            .newBuilder()
            .provider(AuthProvider.LOCAL)
            .sub(UUID.randomUUID().toString())
            .email(EMAIL)
            .givenName(GIVEN_NAME)
            .familyName(FAMILY_NAME)
            .passwordHash(PASSWORD)
            .addRole(AuthRole.USER)
            .build();
        when(userInfoRepository.save(any())).thenReturn(savedUser);

        UserVerification verification = new UserVerification(savedUser, 24);
        when(userVerificationRepository.save(any())).thenReturn(verification);

        // Act
        UserVerification result = userInfoService.registerNewLocalUser(CREATE_USER_REQUEST);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(verification);
    }

    @Test
    void registerNewLocalUser_userExists_throwsException() {
        // Arrange
        when(userInfoRepository.existsByProviderAndEmailIgnoreCase(AuthProvider.LOCAL, EMAIL)).thenReturn(true);

        // Act, Assert
        assertThatThrownBy(() -> userInfoService.registerNewLocalUser(CREATE_USER_REQUEST))
            .isInstanceOf(UserAlreadyExistsException.class);
    }

    @Test
    void findById_returnsFromRepository() {
        // Arrange
        UserInfo user = mock(UserInfo.class);
        when(userInfoRepository.findById(1L)).thenReturn(Optional.of(user));

        // Act, Assert
        assertThat(userInfoService.findById(1L)).contains(user);
    }
}