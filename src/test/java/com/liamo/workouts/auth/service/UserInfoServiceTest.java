package com.liamo.workouts.auth.service;

import com.liamo.workouts.auth.exception.UserAlreadyExistsException;
import com.liamo.workouts.auth.model.AuthProvider;
import com.liamo.workouts.auth.model.AuthRole;
import com.liamo.workouts.auth.model.WorkoutsClaims;
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

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserInfoServiceTest {

    @Mock
    private UserInfoRepository userInfoRepository;
    @Mock
    private UserVerificationRepository userVerificationRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserInfoService userInfoService;

    private final UUID SUB = UUID.randomUUID();
    private final String EMAIL = "bob@example.com";
    private static final String FIRST_NAME = "Bob";
    private static final String LAST_NAME = "Builder";
    private final String PASSWORD = "password";
    private final String ENCODED_PASSWORD = "encodedPassword";

    private final CreateUserRequestDTO CREATE_USER_REQUEST = new CreateUserRequestDTO(
        EMAIL,
        PASSWORD,
        PASSWORD,
        FIRST_NAME,
        LAST_NAME
    );

    @Test
    void registerNewLocalUser_whenUserDoesNotExist_thenSavesUserAndReturnsVerification() {
        when(userInfoRepository.existsByProviderAndEmailIgnoreCase(AuthProvider.LOCAL, EMAIL)).thenReturn(false);
        when(passwordEncoder.encode(PASSWORD)).thenReturn(ENCODED_PASSWORD);

        UserInfo savedUser = UserInfo
            .newBuilder()
            .provider(AuthProvider.LOCAL)
            .sub(SUB.toString())
            .publicId(SUB)
            .email(EMAIL)
            .givenName(FIRST_NAME)
            .familyName(LAST_NAME)
            .passwordHash(ENCODED_PASSWORD)
            .addRole(AuthRole.USER)
            .build();
        when(userInfoRepository.save(any())).thenReturn(savedUser);

        UserVerification userVerification = new UserVerification(savedUser, 24);
        when(userVerificationRepository.save(any())).thenReturn(userVerification);

        UserVerification result = userInfoService.registerNewLocalUser(CREATE_USER_REQUEST);

        assertThat(result).isNotNull();
        assertThat(result.getToken()).isNotNull();
        assertThat(result.getExpiration()).isNotNull();
        assertThat(result.getUserInfo()).isEqualTo(savedUser);
    }

    @Test
    void registerNewLocalUser_whenUserExists_thenThrowsException() {
        when(userInfoRepository.existsByProviderAndEmailIgnoreCase(AuthProvider.LOCAL, EMAIL)).thenReturn(true);

        assertThatThrownBy(() -> userInfoService.registerNewLocalUser(CREATE_USER_REQUEST))
            .isInstanceOf(UserAlreadyExistsException.class);
    }

    @Test
    void findById_thenReturnsFromRepository() {
        UserInfo user = mock(UserInfo.class);
        when(userInfoRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThat(userInfoService.findById(1L)).isEqualTo(Optional.of(user));
    }

    @Test
    void findByPublicId_thenReturnsFromRepository() {
        UUID publicId = UUID.randomUUID();
        UserInfo user = mock(UserInfo.class);
        when(userInfoRepository.findByPublicId(publicId)).thenReturn(Optional.of(user));

        assertThat(userInfoService.findByPublicId(publicId)).isEqualTo(Optional.of(user));
    }

    @Test
    void findByProviderAndEmailIgnoreCase_thenReturnsFromRepository() {
        UserInfo user = mock(UserInfo.class);
        when(userInfoRepository.findByProviderAndEmailIgnoreCase(AuthProvider.LOCAL, EMAIL))
            .thenReturn(Optional.of(user));

        assertThat(userInfoService.findByProviderAndEmailIgnoreCase(AuthProvider.LOCAL, EMAIL))
            .isEqualTo(Optional.of(user));
    }

    @Test
    void findOrRegisterOAuthUser_whenUserDoesNotExist_thenCreatesNewUser() {
        WorkoutsClaims claims = new WorkoutsClaims(
            SUB.toString(),
            EMAIL,
            FIRST_NAME,
            LAST_NAME
        );

        UserInfo user = UserInfo
            .newBuilder()
            .provider(AuthProvider.GOOGLE)
            .sub(SUB.toString())
            .email(EMAIL)
            .givenName(FIRST_NAME)
            .familyName(LAST_NAME)
            .addRole(AuthRole.USER)
            .emailVerified(true)
            .build();


        when(userInfoRepository.findByProviderAndSub(AuthProvider.GOOGLE, SUB.toString()))
            .thenReturn(Optional.empty());
        when(userInfoRepository.save(any())).thenReturn(user);

        UserInfo result = userInfoService.findOrRegisterOAuthUser(AuthProvider.GOOGLE, claims);

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(user);
    }

    @Test
    void findOrRegisterOAuthUser_whenUserExists_thenReturnUserInfo() {
        WorkoutsClaims claims = new WorkoutsClaims(
            SUB.toString(),
            EMAIL,
            FIRST_NAME,
            LAST_NAME
        );

        UserInfo existingUser = UserInfo
            .newBuilder()
            .provider(AuthProvider.GOOGLE)
            .sub(SUB.toString())
            .email(EMAIL)
            .givenName(FIRST_NAME)
            .familyName(LAST_NAME)
            .addRole(AuthRole.USER)
            .emailVerified(true)
            .build();

        when(userInfoRepository.findByProviderAndSub(AuthProvider.GOOGLE, SUB.toString()))
            .thenReturn(Optional.of(existingUser));

        UserInfo result = userInfoService.findOrRegisterOAuthUser(AuthProvider.GOOGLE, claims);

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(existingUser);
    }

    @Test
    void verifyUserEmail_whenInvalidToken_thenReturnsFalse() {
        String invalidToken = "invalidToken";

        assertThat(userInfoService.verifyUserEmail(invalidToken)).isFalse();
    }

    @Test
    void verifyUserEmail_whenNotFoundVerification_thenReturnsFalse() {
        UUID uuid = UUID.randomUUID();
        when(userVerificationRepository.findByToken(uuid)).thenReturn(Optional.empty());

        assertThat(userInfoService.verifyUserEmail(uuid.toString())).isFalse();
    }

    @Test
    void verifyUserEmail_whenVerificationExpired_thenReturnsFalse() {
        UUID uuid = UUID.randomUUID();
        UserInfo user = mock(UserInfo.class);
        UserVerification verification = new UserVerification(user, -1); // expired
        when(userVerificationRepository.findByToken(uuid)).thenReturn(Optional.of(verification));

        assertThat(userInfoService.verifyUserEmail(uuid.toString())).isFalse();
    }

    @Test
    void verifyUserEmail_whenVerificationValid_thenReturnsTrue() {
        UUID uuid = UUID.randomUUID();
        UserInfo user = UserInfo
            .newBuilder()
            .provider(AuthProvider.LOCAL)
            .sub(UUID.randomUUID().toString())
            .email(EMAIL)
            .givenName(FIRST_NAME)
            .familyName(LAST_NAME)
            .passwordHash(ENCODED_PASSWORD)
            .addRole(AuthRole.USER)
            .build();
        UserVerification verification = new UserVerification(user, 24); // valid
        when(userVerificationRepository.findByToken(uuid)).thenReturn(Optional.of(verification));

        assertThat(userInfoService.verifyUserEmail(uuid.toString())).isTrue();
        verify(userVerificationRepository, times(1)).delete(any());
    }

    @Test
    void createNewUserVerification_whenUserDoesNotExist_thenReturnsEmpty() {
        when(userInfoRepository.findByProviderAndEmailIgnoreCase(AuthProvider.LOCAL, EMAIL)).thenReturn(Optional.empty());

        assertThat(userInfoService.createNewUserVerification(EMAIL)).isEmpty();
    }

    @Test
    void createNewUserVerification_whenUserExistsAndEmailVerified_thenReturnsEmpty() {
        UserInfo user = UserInfo
            .newBuilder()
            .provider(AuthProvider.LOCAL)
            .sub(UUID.randomUUID().toString())
            .email(EMAIL)
            .givenName(FIRST_NAME)
            .familyName(LAST_NAME)
            .passwordHash(ENCODED_PASSWORD)
            .addRole(AuthRole.USER)
            .emailVerified(true)
            .build();

        when(userInfoRepository.findByProviderAndEmailIgnoreCase(AuthProvider.LOCAL, EMAIL))
            .thenReturn(Optional.of(user));

        assertThat(userInfoService.createNewUserVerification(EMAIL)).isEmpty();
    }

    @Test
    void createNewUserVerification_whenUserExistsAndEmailNotVerified_thenReturnsVerification() {
        UserInfo user = UserInfo
            .newBuilder()
            .provider(AuthProvider.LOCAL)
            .sub(UUID.randomUUID().toString())
            .email(EMAIL)
            .givenName(FIRST_NAME)
            .familyName(LAST_NAME)
            .passwordHash(ENCODED_PASSWORD)
            .addRole(AuthRole.USER)
            .emailVerified(false)
            .build();

        when(userInfoRepository.findByProviderAndEmailIgnoreCase(AuthProvider.LOCAL, EMAIL))
            .thenReturn(Optional.of(user));

        UserVerification verification = new UserVerification(user, 24);
        when(userVerificationRepository.save(any())).thenReturn(verification);

        Optional<UserVerification> result = userInfoService.createNewUserVerification(EMAIL);

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(verification);
    }
}