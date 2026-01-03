package com.liamo.workouts.auth.model;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents the different types of roles for our user system. Each role contains
 * {@link AuthPermissions} to determine what they can interact with and how.
 */
public enum AuthRole {
    USER(
        Set.of(
            AuthPermissions.Workouts.READ.getPermission(),
            AuthPermissions.Workouts.WRITE.getPermission()
        )
    ),
    ADMIN(
        union(
            USER.getRoles(),
            Set.of(
                AuthPermissions.Users.MANAGE.getPermission()
            )
        )
    );

    private final Set<String> roles;

    AuthRole(Set<String> roles) {
        this.roles = roles;
    }

    public Set<String> getRoles() {
        return roles;
    }

    private static Set<String> union(Set<String> a, Set<String> b) {
        Set<String> result = new HashSet<>(Set.copyOf(a));
        result.addAll(b);
        return result;
    }
}
