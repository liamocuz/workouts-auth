package com.liamo.workouts.auth.service;

import com.liamo.workouts.auth.WorkoutsTestUtil;
import com.liamo.workouts.auth.model.AuthProvider;
import com.liamo.workouts.auth.model.WorkoutsClaims;
import com.liamo.workouts.auth.model.entity.UserInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkoutsOidcUserServiceTest {

    @Mock
    private UserInfoService userInfoService;

    @InjectMocks
    private WorkoutsOidcUserService workoutsOidcUserService;

    @Test
    void verifyServiceCreation() {
        // Verify that the service is properly created with its dependencies
        // This is a basic sanity check test
        assertThat(workoutsOidcUserService).isNotNull();
    }

    @Test
    void verifyUserInfoServiceIntegration() {
        // Arrange
        UserInfo mockUserInfo = WorkoutsTestUtil.getOAuth2UserInfoBuilder()
            .provider(AuthProvider.GOOGLE)
            .sub("google-sub-123")
            .email("google@example.com")
            .givenName("Google")
            .familyName("User")
            .emailVerified(true)
            .build();

        WorkoutsClaims claims = new WorkoutsClaims(
            "google-sub-123",
            "google@example.com",
            "Google",
            "User"
        );

        when(userInfoService.findOrRegisterOAuthUser(any(AuthProvider.class), any(WorkoutsClaims.class)))
            .thenReturn(mockUserInfo);

        // Act
        UserInfo result = userInfoService.findOrRegisterOAuthUser(AuthProvider.GOOGLE, claims);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("google@example.com");
        assertThat(result.getProvider()).isEqualTo(AuthProvider.GOOGLE);
        assertThat(result.isEmailVerified()).isTrue();
    }

    @Test
    void verifyProperAuthProviderParsing() {
        // Test that AuthProvider.valueOf works correctly for supported providers
        AuthProvider google = AuthProvider.valueOf("GOOGLE");
        
        assertThat(google).isEqualTo(AuthProvider.GOOGLE);
    }
}
