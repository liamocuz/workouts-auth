package com.liamo.workouts.auth.model.entity;

import jakarta.persistence.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * Represents a verification token associated with a user, typically used for actions
 * such as email verification or password reset.
 */
@Entity
@Table(name = "user_verification")
public class UserVerification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "token")
    private UUID token;

    @Column(name = "expiration")
    private Instant expiration;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private UserInfo userInfo;

    protected UserVerification() {
    }

    public UserVerification(UserInfo userInfo, long expirationHours) {
        this.userInfo = userInfo;
        this.token = UUID.randomUUID();
        this.expiration = Instant.now().plus(expirationHours, ChronoUnit.HOURS);
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public UUID getToken() {
        return token;
    }

    public Instant getExpiration() {
        return expiration;
    }
}