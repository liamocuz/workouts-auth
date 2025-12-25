package com.liamo.workouts.auth.model;

/**
 * Represents the different types of roles for our user system. Each role contains
 * {@link AuthPermissions} to determine what they can interact with and how.
 */
public enum AuthRole {
    USER(new String[]{
        AuthPermissions.Workouts.READ.getPermission(),
        AuthPermissions.Workouts.WRITE.getPermission()
    }),
    ADMIN(new String[]{
        AuthPermissions.Workouts.READ.getPermission(),
        AuthPermissions.Workouts.WRITE.getPermission(),
        AuthPermissions.Users.MANAGE.getPermission()
    });

    private final String[] roles;

    AuthRole(String[] roles) {
        this.roles = roles;
    }

    public String[] getRoles() {
        return roles;
    }
}
