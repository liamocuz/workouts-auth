package com.liamo.workouts.auth.service;

import com.liamo.workouts.auth.WorkoutsTestUtil;
import com.liamo.workouts.auth.model.AuthProvider;
import com.liamo.workouts.auth.model.WorkoutsClaims;
import com.liamo.workouts.auth.model.entity.UserInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkoutsOidcUserServiceTest {

    @Mock
    private UserInfoService userInfoService;

    private WorkoutsOidcUserService workoutsOidcUserService;

    @BeforeEach
    void setUp() {
        workoutsOidcUserService = spy(new WorkoutsOidcUserService(userInfoService));
    }

    @Test
    void loadUser_whenGoogleUser_thenExtractsClaimsAndCreatesUserInfo() {
        // Arrange
        ClientRegistration clientRegistration = ClientRegistration
            .withRegistrationId("google")
            .clientId("test-client-id")
            .clientSecret("test-client-secret")
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
            .scope("openid", "email", "profile")
            .authorizationUri("https://accounts.google.com/o/oauth2/auth")
            .tokenUri("https://oauth2.googleapis.com/token")
            .userInfoUri("https://openidconnect.googleapis.com/v1/userinfo")
            .userNameAttributeName("sub")
            .jwkSetUri("https://www.googleapis.com/oauth2/v3/certs")
            .build();

        OAuth2AccessToken accessToken = new OAuth2AccessToken(
            OAuth2AccessToken.TokenType.BEARER,
            "token-value",
            Instant.now(),
            Instant.now().plusSeconds(3600)
        );

        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "google-123");
        claims.put("email", "user@google.com");
        claims.put("given_name", "Alice");
        claims.put("family_name", "Wonder");
        claims.put("email_verified", true);

        OidcIdToken idToken = new OidcIdToken(
            "token-value",
            Instant.now(),
            Instant.now().plusSeconds(3600),
            claims
        );

        OidcUserRequest userRequest = new OidcUserRequest(clientRegistration, accessToken, idToken);

        // Mock the OIDC user returned by parent class
        OidcUser mockOidcUser = new DefaultOidcUser(
            Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
            idToken
        );

        doReturn(mockOidcUser).when(workoutsOidcUserService).loadUser(userRequest);

        // Mock UserInfo from database
        UserInfo mockUserInfo = WorkoutsTestUtil.getOAuth2UserInfoBuilder()
            .provider(AuthProvider.GOOGLE)
            .sub("google-123")
            .email("user@google.com")
            .givenName("Alice")
            .familyName("Wonder")
            .emailVerified(true)
            .build();

        when(userInfoService.findOrRegisterOAuthUser(eq(AuthProvider.GOOGLE), any(WorkoutsClaims.class)))
            .thenReturn(mockUserInfo);

        // Act
        OidcUser result = workoutsOidcUserService.loadUser(userRequest);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getSubject()).isEqualTo("google-123");
        assertThat(result.getEmail()).isEqualTo("user@google.com");
    }

    @Test
    void loadUser_whenCalled_thenInvokesUserInfoService() {
        // Arrange
        ClientRegistration clientRegistration = ClientRegistration
            .withRegistrationId("google")
            .clientId("test-client-id")
            .clientSecret("test-client-secret")
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
            .scope("openid", "email", "profile")
            .authorizationUri("https://accounts.google.com/o/oauth2/auth")
            .tokenUri("https://oauth2.googleapis.com/token")
            .userInfoUri("https://openidconnect.googleapis.com/v1/userinfo")
            .userNameAttributeName("sub")
            .jwkSetUri("https://www.googleapis.com/oauth2/v3/certs")
            .build();

        OAuth2AccessToken accessToken = new OAuth2AccessToken(
            OAuth2AccessToken.TokenType.BEARER,
            "token-value",
            Instant.now(),
            Instant.now().plusSeconds(3600)
        );

        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "google-456");
        claims.put("email", "test@google.com");
        claims.put("given_name", "Bob");
        claims.put("family_name", "Builder");
        claims.put("email_verified", true);

        OidcIdToken idToken = new OidcIdToken(
            "token-value",
            Instant.now(),
            Instant.now().plusSeconds(3600),
            claims
        );

        OidcUserRequest userRequest = new OidcUserRequest(clientRegistration, accessToken, idToken);

        OidcUser mockOidcUser = new DefaultOidcUser(
            Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
            idToken
        );

        doReturn(mockOidcUser).when(workoutsOidcUserService).loadUser(userRequest);

        UserInfo mockUserInfo = WorkoutsTestUtil.getOAuth2UserInfoBuilder()
            .provider(AuthProvider.GOOGLE)
            .sub("google-456")
            .email("test@google.com")
            .givenName("Bob")
            .familyName("Builder")
            .emailVerified(true)
            .build();

        ArgumentCaptor<WorkoutsClaims> claimsCaptor = ArgumentCaptor.forClass(WorkoutsClaims.class);
        when(userInfoService.findOrRegisterOAuthUser(eq(AuthProvider.GOOGLE), claimsCaptor.capture()))
            .thenReturn(mockUserInfo);

        // Act
        workoutsOidcUserService.loadUser(userRequest);

        // Assert - verify that userInfoService was called
        verify(userInfoService, times(1)).findOrRegisterOAuthUser(eq(AuthProvider.GOOGLE), any(WorkoutsClaims.class));
    }

    @Test
    void loadUser_whenProviderIsGoogle_thenParsesProviderCorrectly() {
        // Arrange
        ClientRegistration clientRegistration = ClientRegistration
            .withRegistrationId("google")
            .clientId("test-client-id")
            .clientSecret("test-client-secret")
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
            .scope("openid", "email", "profile")
            .authorizationUri("https://accounts.google.com/o/oauth2/auth")
            .tokenUri("https://oauth2.googleapis.com/token")
            .userInfoUri("https://openidconnect.googleapis.com/v1/userinfo")
            .userNameAttributeName("sub")
            .jwkSetUri("https://www.googleapis.com/oauth2/v3/certs")
            .build();

        OAuth2AccessToken accessToken = new OAuth2AccessToken(
            OAuth2AccessToken.TokenType.BEARER,
            "token-value",
            Instant.now(),
            Instant.now().plusSeconds(3600)
        );

        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "google-789");
        claims.put("email", "provider@test.com");
        claims.put("given_name", "Test");
        claims.put("family_name", "User");
        claims.put("email_verified", true);

        OidcIdToken idToken = new OidcIdToken(
            "token-value",
            Instant.now(),
            Instant.now().plusSeconds(3600),
            claims
        );

        OidcUserRequest userRequest = new OidcUserRequest(clientRegistration, accessToken, idToken);

        OidcUser mockOidcUser = new DefaultOidcUser(
            Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
            idToken
        );

        doReturn(mockOidcUser).when(workoutsOidcUserService).loadUser(userRequest);

        UserInfo mockUserInfo = WorkoutsTestUtil.getOAuth2UserInfoBuilder()
            .provider(AuthProvider.GOOGLE)
            .sub("google-789")
            .build();

        ArgumentCaptor<AuthProvider> providerCaptor = ArgumentCaptor.forClass(AuthProvider.class);
        when(userInfoService.findOrRegisterOAuthUser(providerCaptor.capture(), any(WorkoutsClaims.class)))
            .thenReturn(mockUserInfo);

        // Act
        workoutsOidcUserService.loadUser(userRequest);

        // Assert - verify AuthProvider.GOOGLE was used
        verify(userInfoService).findOrRegisterOAuthUser(eq(AuthProvider.GOOGLE), any(WorkoutsClaims.class));
        assertThat(providerCaptor.getValue()).isEqualTo(AuthProvider.GOOGLE);
    }
}
