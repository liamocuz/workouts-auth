package com.liamo.workouts.auth.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AuthPermissionsTest {

    @Test
    void workoutsReadPermission_shouldReturnCorrectFormat() {
        // Act
        String permission = AuthPermissions.Workouts.READ.getPermission();

        // Assert
        assertThat(permission).isEqualTo("workouts.read");
    }

    @Test
    void workoutsWritePermission_shouldReturnCorrectFormat() {
        // Act
        String permission = AuthPermissions.Workouts.WRITE.getPermission();

        // Assert
        assertThat(permission).isEqualTo("workouts.write");
    }

    @Test
    void workoutsDeletePermission_shouldReturnCorrectFormat() {
        // Act
        String permission = AuthPermissions.Workouts.DELETE.getPermission();

        // Assert
        assertThat(permission).isEqualTo("workouts.delete");
    }

    @Test
    void usersManagePermission_shouldReturnCorrectFormat() {
        // Act
        String permission = AuthPermissions.Users.MANAGE.getPermission();

        // Assert
        assertThat(permission).isEqualTo("users.manage");
    }

    @Test
    void allWorkoutsPermissions_shouldUseLowercase() {
        // Act & Assert
        for (AuthPermissions.Workouts perm : AuthPermissions.Workouts.values()) {
            assertThat(perm.getPermission())
                .startsWith("workouts.")
                .isLowerCase();
        }
    }

    @Test
    void allUsersPermissions_shouldUseLowercase() {
        // Act & Assert
        for (AuthPermissions.Users perm : AuthPermissions.Users.values()) {
            assertThat(perm.getPermission())
                .startsWith("users.")
                .isLowerCase();
        }
    }

    @Test
    void permissionFormat_shouldFollowDomainDotActionPattern() {
        // Assert - Verify all permissions follow the "domain.action" format
        assertThat(AuthPermissions.Workouts.READ.getPermission()).matches("^[a-z]+\\.[a-z]+$");
        assertThat(AuthPermissions.Workouts.WRITE.getPermission()).matches("^[a-z]+\\.[a-z]+$");
        assertThat(AuthPermissions.Workouts.DELETE.getPermission()).matches("^[a-z]+\\.[a-z]+$");
        assertThat(AuthPermissions.Users.MANAGE.getPermission()).matches("^[a-z]+\\.[a-z]+$");
    }
}
