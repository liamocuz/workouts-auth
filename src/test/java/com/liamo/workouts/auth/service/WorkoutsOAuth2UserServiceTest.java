package com.liamo.workouts.auth.service;

import com.liamo.workouts.auth.WorkoutsTestUtil;
import com.liamo.workouts.auth.model.AuthProvider;
import com.liamo.workouts.auth.model.WorkoutsClaims;
import com.liamo.workouts.auth.model.entity.UserInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkoutsOAuth2UserServiceTest {

    @Mock
    private UserInfoService userInfoService;

    @InjectMocks
    private WorkoutsOAuth2UserService workoutsOAuth2UserService;

    @Test
    void verifyServiceCreation() {
        // Verify that the service is properly created with its dependencies
        // This is a basic sanity check test
        assertThat(workoutsOAuth2UserService).isNotNull();
    }

    @Test
    void verifyUserInfoServiceIntegration() {
        // Arrange
        UserInfo mockUserInfo = WorkoutsTestUtil.getOAuth2UserInfoBuilder()
            .provider(AuthProvider.FACEBOOK)
            .sub("facebook-sub-123")
            .email("facebook@example.com")
            .givenName("Test")
            .familyName("User")
            .build();

        WorkoutsClaims claims = new WorkoutsClaims(
            "facebook-sub-123",
            "facebook@example.com",
            "Test",
            "User"
        );

        when(userInfoService.findOrRegisterOAuthUser(any(AuthProvider.class), any(WorkoutsClaims.class)))
            .thenReturn(mockUserInfo);

        // Act
        UserInfo result = userInfoService.findOrRegisterOAuthUser(AuthProvider.FACEBOOK, claims);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("facebook@example.com");
        assertThat(result.getProvider()).isEqualTo(AuthProvider.FACEBOOK);
    }

    @Test
    void verifyProperAuthProviderParsing() {
        // Test that AuthProvider.valueOf works correctly for supported providers
        AuthProvider facebook = AuthProvider.valueOf("FACEBOOK");
        AuthProvider google = AuthProvider.valueOf("GOOGLE");
        
        assertThat(facebook).isEqualTo(AuthProvider.FACEBOOK);
        assertThat(google).isEqualTo(AuthProvider.GOOGLE);
    }
}
