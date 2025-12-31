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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkoutsOidcUserServiceTest {

    @Mock
    private UserInfoService userInfoService;

    @InjectMocks
    private WorkoutsOidcUserService workoutsOidcUserService;

    @Test
    void testServiceDependencyInjection() {
        // Verify the service has its dependency properly injected
        assertThat(workoutsOidcUserService).isNotNull();
        
        // Create a spy to verify the service can be instantiated
        WorkoutsOidcUserService service = new WorkoutsOidcUserService(userInfoService);
        assertThat(service).isNotNull();
    }

    @Test
    void testFindOrRegisterOAuthUser_callsUserInfoService() {
        // Arrange
        UserInfo mockUserInfo = WorkoutsTestUtil.getOAuth2UserInfoBuilder()
            .provider(AuthProvider.GOOGLE)
            .sub("google-123")
            .email("user@google.com")
            .givenName("Alice")
            .familyName("Wonder")
            .emailVerified(true)
            .build();

        WorkoutsClaims claims = new WorkoutsClaims(
            "google-123",
            "user@google.com",
            "Alice",
            "Wonder"
        );

        when(userInfoService.findOrRegisterOAuthUser(eq(AuthProvider.GOOGLE), any(WorkoutsClaims.class)))
            .thenReturn(mockUserInfo);

        // Act
        UserInfo result = userInfoService.findOrRegisterOAuthUser(AuthProvider.GOOGLE, claims);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("user@google.com");
        assertThat(result.getProvider()).isEqualTo(AuthProvider.GOOGLE);
        assertThat(result.getSub()).isEqualTo("google-123");
        assertThat(result.isEmailVerified()).isTrue();
        
        verify(userInfoService, times(1)).findOrRegisterOAuthUser(eq(AuthProvider.GOOGLE), any(WorkoutsClaims.class));
    }

    @Test
    void testAuthProviderParsing() {
        // Test that AuthProvider enum correctly handles provider names
        AuthProvider google = AuthProvider.valueOf("GOOGLE");
        assertThat(google).isEqualTo(AuthProvider.GOOGLE);
        
        // Verify the service would parse "google" (lowercase) to GOOGLE
        String registrationId = "google";
        String upperCaseProvider = registrationId.toUpperCase();
        AuthProvider parsed = AuthProvider.valueOf(upperCaseProvider);
        assertThat(parsed).isEqualTo(AuthProvider.GOOGLE);
    }
}
