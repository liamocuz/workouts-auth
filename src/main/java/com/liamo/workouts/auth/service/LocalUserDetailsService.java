package com.liamo.workouts.auth.service;

import com.liamo.workouts.auth.model.AuthProvider;
import com.liamo.workouts.auth.model.AuthRole;
import com.liamo.workouts.auth.model.entity.UserInfo;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Set;

/**
 * A service implementing {@link UserDetailsService} by fetching a {@link UserInfo} entity
 * and returning its information.
 */
@Service
public class LocalUserDetailsService implements UserDetailsService {
    private final UserInfoService userInfoService;

    public LocalUserDetailsService(UserInfoService userInfoService) {
        this.userInfoService = userInfoService;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserInfo user = userInfoService.findByProviderAndEmailIgnoreCase(AuthProvider.LOCAL, email)
            .orElseThrow(() -> new UsernameNotFoundException("No LOCAL user: " + email));

        String publicId = user.getPublicId().toString();
        return User.withUsername(publicId)
            .password(user.getPasswordHash())
            .authorities(getAuthorities(user.getRoles()))
            .disabled(!user.isEnabled())
            .accountLocked(!user.isEmailVerified())
            .build();
    }

    private static Collection<? extends GrantedAuthority> getAuthorities(Set<AuthRole> roles) {
        return roles.stream()
            .map(role -> new SimpleGrantedAuthority(role.name()))
            .toList();
    }
}
