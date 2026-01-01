package com.liamo.workouts.auth.model;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class WorkoutsClaimsTest {

    @Test
    void getClaimsAttributes_shouldReturnAllClaimsAsMap() {
        // Arrange
        String sub = "user-123";
        String email = "test@example.com";
        String givenName = "John";
        String familyName = "Doe";
        
        WorkoutsClaims claims = new WorkoutsClaims(sub, email, givenName, familyName);

        // Act
        Map<String, Object> attributes = claims.getClaimsAttributes();

        // Assert
        assertThat(attributes).isNotNull();
        assertThat(attributes).hasSize(4);
        assertThat(attributes.get("sub")).isEqualTo(sub);
        assertThat(attributes.get("email")).isEqualTo(email);
        assertThat(attributes.get("given_name")).isEqualTo(givenName);
        assertThat(attributes.get("family_name")).isEqualTo(familyName);
    }

    @Test
    void getClaimsAttributes_shouldUseSnakeCaseForNames() {
        // Arrange
        WorkoutsClaims claims = new WorkoutsClaims("sub", "email", "given", "family");

        // Act
        Map<String, Object> attributes = claims.getClaimsAttributes();

        // Assert - Verify snake_case keys are used for OIDC compliance
        assertThat(attributes).containsKey("given_name");
        assertThat(attributes).containsKey("family_name");
        assertThat(attributes).doesNotContainKey("givenName");
        assertThat(attributes).doesNotContainKey("familyName");
    }

    @Test
    void constructor_shouldStoreAllFields() {
        // Arrange & Act
        WorkoutsClaims claims = new WorkoutsClaims("sub-id", "user@test.com", "Jane", "Smith");

        // Assert
        assertThat(claims.sub()).isEqualTo("sub-id");
        assertThat(claims.email()).isEqualTo("user@test.com");
        assertThat(claims.givenName()).isEqualTo("Jane");
        assertThat(claims.familyName()).isEqualTo("Smith");
    }

    @Test
    void getClaimsAttributes_withNullValues_shouldIncludeNulls() {
        // Arrange
        WorkoutsClaims claims = new WorkoutsClaims("sub", null, null, null);

        // Act
        Map<String, Object> attributes = claims.getClaimsAttributes();

        // Assert - Verify nulls are preserved in the map
        assertThat(attributes).containsKey("email");
        assertThat(attributes).containsKey("given_name");
        assertThat(attributes).containsKey("family_name");
        assertThat(attributes.get("email")).isNull();
        assertThat(attributes.get("given_name")).isNull();
        assertThat(attributes.get("family_name")).isNull();
    }
}
