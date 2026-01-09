package com.liamo.workouts.auth.service;

import com.liamo.workouts.auth.model.AuthProvider;
import com.liamo.workouts.auth.model.WorkoutsClaims;
import com.liamo.workouts.auth.model.entity.UserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;


/**
 * A service to create an {@link OidcUser} by fetching a {@link UserInfo} entity
 * and merging its information.
 */
@Service
public class WorkoutsOidcUserService extends OidcUserService {
    private static final Logger logger = LoggerFactory.getLogger(WorkoutsOidcUserService.class);

    private final UserInfoService userInfoService;

    public WorkoutsOidcUserService(UserInfoService userInfoService) {
        this.userInfoService = userInfoService;
    }

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        logger.debug("Loading Oidc User");

        OidcUser oidcUser = super.loadUser(userRequest);

        String provider = userRequest.getClientRegistration().getRegistrationId().toUpperCase();
        AuthProvider authProvider = AuthProvider.valueOf(provider);

        logger.debug("Found Oidc User: {}", oidcUser);
        logger.debug("Found Oidc User attributes: {}", oidcUser.getAttributes());
        logger.debug("Found Oidc User authorities: {}", oidcUser.getAuthorities());
        logger.debug("Found Oidc User userInfo: {}", oidcUser.getUserInfo());
        logger.debug("Found Oidc User idToken: {}", oidcUser.getIdToken());
        logger.debug("Found Oidc User claims: {}", oidcUser.getClaims());

        WorkoutsClaims workoutsClaims = new WorkoutsClaims(
            oidcUser.getSubject(),
            oidcUser.getEmail(),
            oidcUser.getGivenName(),
            oidcUser.getFamilyName()
        );

        UserInfo userInfo = userInfoService.findOrRegisterOAuthUser(authProvider, workoutsClaims);

        logger.debug("Oidc UserInfo: {}", userInfo);
        Map<String, Object> claimsMap = new HashMap<>();
        claimsMap.put("public_id", userInfo.getPublicId().toString());
        claimsMap.put("email", userInfo.getEmail());
        claimsMap.put("given_name", userInfo.getGivenName());
        claimsMap.put("family_name", userInfo.getFamilyName());

        OidcUserInfo oidcUserInfo = new OidcUserInfo(claimsMap);

        return new DefaultOidcUser(
            oidcUser.getAuthorities(),
            oidcUser.getIdToken(),
            oidcUserInfo,
            "public_id");
    }
}
