package com.liamo.workouts.auth.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Claims to be included in the Workouts ID Token.
 *
 * @param sub        The subject - the user's unique ID (public identifier)
 * @param email      The user's email
 * @param givenName  The user's first name
 * @param familyName The user's last name
 */
public record WorkoutsClaims(
    String sub,
    String email,
    String givenName,
    String familyName
) {

    public Map<String, Object> getClaimsAttributes() {
        Map<String, Object> claimsMap = new HashMap<>();
        claimsMap.put("sub", sub);
        claimsMap.put("email", email);
        claimsMap.put("given_name", givenName);
        claimsMap.put("family_name", familyName);
        return claimsMap;
    }
}
