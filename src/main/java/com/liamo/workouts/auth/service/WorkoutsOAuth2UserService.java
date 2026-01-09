package com.liamo.workouts.auth.service;

import com.liamo.workouts.auth.model.AuthProvider;
import com.liamo.workouts.auth.model.WorkoutsClaims;
import com.liamo.workouts.auth.model.entity.UserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * A service to create an {@link OAuth2User} by fetching a {@link UserInfo} entity
 * and merging its information.
 */
@Service
public class WorkoutsOAuth2UserService extends DefaultOAuth2UserService {
    private static final Logger logger = LoggerFactory.getLogger(WorkoutsOAuth2UserService.class);

    private final UserInfoService userInfoService;

    public WorkoutsOAuth2UserService(UserInfoService userInfoService) {
        this.userInfoService = userInfoService;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        logger.debug("Loading OAuth2 User");

        OAuth2User oAuth2User = super.loadUser(userRequest);

        String provider = userRequest.getClientRegistration().getRegistrationId().toUpperCase();
        AuthProvider authProvider = AuthProvider.valueOf(provider);

        logger.debug("Found OAuth2 User: {}", oAuth2User);
        logger.debug("Found OAuth2 User attributes: {}", oAuth2User.getAttributes());

        // These works for Facebook, but probably would not work for all other OAuth2 providers
        String subject = oAuth2User.getAttribute("id");
        String email = oAuth2User.getAttribute("email");
        String givenName = oAuth2User.getAttribute("first_name");
        String familyName = oAuth2User.getAttribute("last_name");

        WorkoutsClaims claims = new WorkoutsClaims(
            subject,
            email,
            givenName,
            familyName
        );

        UserInfo userInfo = userInfoService.findOrRegisterOAuthUser(authProvider, claims);

        logger.debug("OAuth2 UserInfo: {}", userInfo);
        Map<String, Object> claimsMap = claims.getClaimsAttributes();
        claimsMap.put("public_id", userInfo.getPublicId().toString());

        return new DefaultOAuth2User(
            oAuth2User.getAuthorities(),
            claimsMap,
            "public_id"
        );
    }
}
