package com.liamo.workouts.auth.model.entity;

import com.liamo.workouts.auth.model.AuthRole;
import com.liamo.workouts.auth.model.AuthProvider;
import com.liamo.workouts.auth.model.validation.UserInfoBuilderValidator;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SourceType;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.*;

/**
 * Represents a User in our management system. This user can either be registered locally
 * or be a user delegated to an OAuth2 provider.
 */
@Entity
@Table(
    name = "user_info",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"sub", "provider"}),
        @UniqueConstraint(columnNames = {"email", "provider"}),
        @UniqueConstraint(columnNames = {"public_id"})
    }
)
public class UserInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * An identifier we can safely surface to the frontend. This value is used
     * as the sub claim in our JWT tokens.
     */
    @Column(name = "public_id", unique = true, nullable = false)
    private UUID publicId;

    @Column(name = "email", nullable = false)
    private String email;

    /**
     * The sub will be a random UUID that does not match the publicId for a LOCAL user,
     * and will be the providers sub for all OIDC/OAuth2 users.
     */
    @Column(name = "sub", nullable = false)
    private String sub;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "provider", nullable = false)
    private AuthProvider provider;

    @Column(name = "given_name")
    private String givenName;

    @Column(name = "family_name")
    private String familyName;

    @Column(name = "display_name")
    private String displayName;

    @Column(name = "password_hash")
    private String passwordHash;    // Only for LOCAL accounts

    @CreationTimestamp(source = SourceType.DB)
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @UpdateTimestamp(source = SourceType.DB)
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "last_login")
    private Instant lastLogin;

    @Column(name = "deleted_at")
    private Instant deletedAt;  // For soft-deletion mechanism

    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified = false;

    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(value = EnumType.STRING)
    @Column(name = "role")
    private Set<AuthRole> roles = new HashSet<>();

    private UserInfo(Builder builder) {
        id = builder.id;
        publicId = builder.publicId;
        provider = builder.provider;
        email = builder.email;
        sub = builder.sub;
        givenName = builder.givenName;
        familyName = builder.familyName;
        displayName = builder.displayName;
        passwordHash = builder.passwordHash;
        emailVerified = builder.emailVerified;
        enabled = builder.enabled;
        roles = builder.roles;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    protected UserInfo() {
    }

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof UserInfo userInfo)) return false;

        return sub.equals(userInfo.sub) && provider == userInfo.provider;
    }

    @Override
    public int hashCode() {
        int result = sub.hashCode();
        result = 31 * result + provider.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "UserInfo{" +
            "id=" + id +
            ", publicId=" + publicId +
            ", email='" + email + '\'' +
            ", sub='" + sub + '\'' +
            ", provider=" + provider +
            ", givenName='" + givenName + '\'' +
            ", familyName='" + familyName + '\'' +
            ", displayName='" + displayName + '\'' +
            ", passwordHash='" + passwordHash + '\'' +
            ", emailVerified=" + emailVerified +
            ", createdAt=" + createdAt +
            ", updatedAt=" + updatedAt +
            ", lastLogin=" + lastLogin +
            ", deletedAt=" + deletedAt +
            ", enabled=" + enabled +
            ", roles=" + roles +
            '}';
    }

    // Setters

    public void setEmail(String email) {
        this.email = email;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void addRole(AuthRole authRole) {
        roles.add(authRole);
    }

    public void removeRole(AuthRole authRole) {
        roles.remove(authRole);
    }

    public void setLastLogin(Instant lastLogin) {
        this.lastLogin = lastLogin;
    }

    // Getters

    public Long getId() {
        return id;
    }

    public UUID getPublicId() {
        return publicId;
    }

    public String getEmail() {
        return email;
    }

    public String getSub() {
        return sub;
    }

    public AuthProvider getProvider() {
        return provider;
    }

    public String getGivenName() {
        return givenName;
    }

    public String getFamilyName() {
        return familyName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Instant getLastLogin() {
        return lastLogin;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Set<AuthRole> getRoles() {
        return Set.copyOf(roles);
    }


    //@formatter:off
    public static final class Builder {
        private Long id;
        private UUID publicId;
        private String email;
        private String sub;
        private AuthProvider provider;
        private String givenName;
        private String familyName;
        private String displayName;
        private String passwordHash;
        private boolean emailVerified = false;
        private boolean enabled = true;
        private Set<AuthRole> roles = new HashSet<>();

        private Builder() {}

        public Builder id(Long val) {id = val; return this;}
        public Builder publicId(UUID val) {publicId = val; return this;}
        public Builder email(String val) {email = val; return this;}
        public Builder sub(String val) {sub = val; return this;}
        public Builder provider(AuthProvider val) {provider = val; return this;}
        public Builder givenName(String val) {givenName = val; return this;}
        public Builder familyName(String val) {familyName = val; return this;}
        public Builder displayName(String val) {displayName = val; return this;}
        public Builder passwordHash(String val) {passwordHash = val; return this;}
        public Builder emailVerified(boolean val) {emailVerified = val; return this;}
        public Builder enabled(boolean val) {enabled = val; return this;}
        public Builder roles(Set<AuthRole> val) {roles = val; return this;}
        public Builder addRole(AuthRole val) {roles.add(val); return this;}

        public Long getId() {return id;}
        public UUID getPublicId() {return publicId;}
        public String getEmail() {return email;}
        public String getSub() {return sub;}
        public AuthProvider getProvider() {return provider;}
        public String getGivenName() {return givenName;}
        public String getFamilyName() {return familyName;}
        public String getDisplayName() {return displayName;}
        public String getPasswordHash() {return passwordHash;}
        public boolean isEmailVerified() {return emailVerified;}
        public boolean isEnabled() {return enabled;}
        public Set<AuthRole> getRoles() {return Set.copyOf(roles);}

        public UserInfo build() {
            UserInfoBuilderValidator.validate(this);

            // TODO: Allow user to change their display name
            this.displayName(this.givenName);

            // Should only set for OAuth2 users, LOCAL sets its sub and publicId the same
            if (this.publicId == null)
                this.publicId(UUID.randomUUID());

            return new UserInfo(this);
        }
    }
    //@formatter:on
}
