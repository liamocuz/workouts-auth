package com.liamo.workouts.auth;

import com.liamo.workouts.auth.model.AuthProvider;
import com.liamo.workouts.auth.model.AuthRole;
import com.liamo.workouts.auth.model.entity.UserInfo;

import java.util.UUID;

public class WorkoutsTestUtil {

    public static final String EMAIL = "bob@builder.com";
    public static final String GIVEN_NAME = "Bob";
    public static final String FAMILY_NAME = "Builder";
    public static final String PASSWORD = "password";
    public static final String PASSWORD_HASH = "password-hash";
    public static final String OAUTH2_SUB = "oauth2-sub-id";
    public static final UUID PUBLIC_ID = UUID.randomUUID();

    public static UserInfo.Builder getLocalUserInfoBuilder() {
        return UserInfo
            .newBuilder()
            .provider(AuthProvider.LOCAL)
            .sub(PUBLIC_ID.toString())
            .publicId(PUBLIC_ID)
            .email(EMAIL)
            .givenName(GIVEN_NAME)
            .familyName(FAMILY_NAME)
            .addRole(AuthRole.ADMIN)
            .passwordHash(PASSWORD_HASH);
    }

    public static UserInfo.Builder getOAuth2UserInfoBuilder() {
        return UserInfo
            .newBuilder()
            .provider(AuthProvider.GOOGLE)
            .sub(OAUTH2_SUB)
            .publicId(PUBLIC_ID)
            .email(EMAIL)
            .givenName(GIVEN_NAME)
            .familyName(FAMILY_NAME)
            .addRole(AuthRole.USER);
    }
}
