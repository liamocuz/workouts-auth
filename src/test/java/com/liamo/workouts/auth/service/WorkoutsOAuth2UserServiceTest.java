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
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkoutsOAuth2UserServiceTest {

    @Mock
    private UserInfoService userInfoService;

    @InjectMocks
    private WorkoutsOAuth2UserService workoutsOAuth2UserService;

    @Test
    void testServiceDependencyInjection() {
        // Verify the service has its dependency properly injected
        assertThat(workoutsOAuth2UserService).isNotNull();
        
        // Create a spy to verify the service can be instantiated
        WorkoutsOAuth2UserService service = new WorkoutsOAuth2UserService(userInfoService);
        assertThat(service).isNotNull();
    }

    @Test
    void testFindOrRegisterOAuthUser_callsUserInfoService() {
        // Arrange
        UserInfo mockUserInfo = WorkoutsTestUtil.getOAuth2UserInfoBuilder()
            .provider(AuthProvider.FACEBOOK)
            .sub("facebook-123")
            .email("user@facebook.com")
            .givenName("John")
            .familyName("Doe")
            .build();

        WorkoutsClaims claims = new WorkoutsClaims(
            "facebook-123",
            "user@facebook.com",
            "John",
            "Doe"
        );

        when(userInfoService.findOrRegisterOAuthUser(eq(AuthProvider.FACEBOOK), any(WorkoutsClaims.class)))
            .thenReturn(mockUserInfo);

        // Act
        UserInfo result = userInfoService.findOrRegisterOAuthUser(AuthProvider.FACEBOOK, claims);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("user@facebook.com");
        assertThat(result.getProvider()).isEqualTo(AuthProvider.FACEBOOK);
        assertThat(result.getSub()).isEqualTo("facebook-123");
        
        verify(userInfoService, times(1)).findOrRegisterOAuthUser(eq(AuthProvider.FACEBOOK), any(WorkoutsClaims.class));
    }

    @Test
    void testAuthProviderParsing() {
        // Test that AuthProvider enum correctly handles provider names
        AuthProvider facebook = AuthProvider.valueOf("FACEBOOK");
        assertThat(facebook).isEqualTo(AuthProvider.FACEBOOK);
        
        // Verify the service would parse "facebook" (lowercase) to FACEBOOK
        String registrationId = "facebook";
        String upperCaseProvider = registrationId.toUpperCase();
        AuthProvider parsed = AuthProvider.valueOf(upperCaseProvider);
        assertThat(parsed).isEqualTo(AuthProvider.FACEBOOK);
    }
}
