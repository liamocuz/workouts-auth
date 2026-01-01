package com.liamo.workouts.auth.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AuthRoleTest {

    @Test
    void userRole_shouldHaveWorkoutsPermissions() {
        // Act
        String[] roles = AuthRole.USER.getRoles();

        // Assert
        assertThat(roles).isNotNull();
        assertThat(roles).contains(
            "workouts.read",
            "workouts.write"
        );
    }

    @Test
    void adminRole_shouldHaveAllWorkoutsAndUserManagementPermissions() {
        // Act
        String[] roles = AuthRole.ADMIN.getRoles();

        // Assert
        assertThat(roles).isNotNull();
        assertThat(roles).contains(
            "workouts.read",
            "workouts.write",
            "users.manage"
        );
    }

    @Test
    void adminRole_shouldHaveMorePermissionsThanUser() {
        // Act
        String[] userRoles = AuthRole.USER.getRoles();
        String[] adminRoles = AuthRole.ADMIN.getRoles();

        // Assert
        assertThat(adminRoles).hasSizeGreaterThan(userRoles.length);
    }

    @Test
    void userRole_shouldNotHaveUserManagementPermission() {
        // Act
        String[] roles = AuthRole.USER.getRoles();

        // Assert
        assertThat(roles).doesNotContain("users.manage");
    }

    @Test
    void getRoles_shouldReturnNonEmptyArray() {
        // Assert - Verify all roles have permissions
        assertThat(AuthRole.USER.getRoles()).isNotEmpty();
        assertThat(AuthRole.ADMIN.getRoles()).isNotEmpty();
    }
}
