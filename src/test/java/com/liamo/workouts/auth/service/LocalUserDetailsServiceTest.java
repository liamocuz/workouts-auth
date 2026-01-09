package com.liamo.workouts.auth.service;

import com.liamo.workouts.auth.model.AuthProvider;
import com.liamo.workouts.auth.model.AuthRole;
import com.liamo.workouts.auth.model.entity.UserInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LocalUserDetailsServiceTest {

    private static final String EMAIL = "bob@example.com";
    private static final String FIRST_NAME = "Bob";
    private static final String LAST_NAME = "Builder";
    private static final String PASSWORD_HASH = "passwordHash";

    @Mock
    private UserInfoService userInfoService;

    @InjectMocks
    private LocalUserDetailsService userDetailsService;

    @Test
    void loadByUsername_whenUserDoesNotExist_thenThrowException() {
        when(userInfoService.findByProviderAndEmailIgnoreCase(AuthProvider.LOCAL, EMAIL))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername(EMAIL))
            .isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    void loadByUsername_whenUserExists_thenReturnUserDetails() {
        UUID userId = UUID.randomUUID();
        UserInfo user = UserInfo
            .newBuilder()
            .sub(userId.toString())
            .publicId(userId)
            .provider(AuthProvider.LOCAL)
            .email(EMAIL)
            .givenName(FIRST_NAME)
            .familyName(LAST_NAME)
            .passwordHash(PASSWORD_HASH)
            .addRole(AuthRole.USER)
            .addRole(AuthRole.ADMIN)
            .build();

        when(userInfoService.findByProviderAndEmailIgnoreCase(AuthProvider.LOCAL, EMAIL))
            .thenReturn(Optional.of(user));

        var userDetails = userDetailsService.loadUserByUsername(EMAIL);

        assertThat(userDetails.getUsername()).isEqualTo(userId.toString());
        assertThat(userDetails.getPassword()).isEqualTo(PASSWORD_HASH);

        assertThat(userDetails.getAuthorities()).hasSize(2);

        assertThat(userDetails.getAuthorities())
            .extracting("authority")
            .containsExactlyInAnyOrder(AuthRole.USER.name(), AuthRole.ADMIN.name());

        assertThat(userDetails.isEnabled()).isTrue();
        assertThat(userDetails.isAccountNonExpired()).isTrue();
        assertThat(userDetails.isAccountNonLocked()).isTrue();
        assertThat(userDetails.isCredentialsNonExpired()).isTrue();
    }

}