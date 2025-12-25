package com.liamo.workouts.auth.service;

import com.liamo.workouts.auth.exception.UserAlreadyExistsException;
import com.liamo.workouts.auth.model.AuthProvider;
import com.liamo.workouts.auth.model.AuthRole;
import com.liamo.workouts.auth.model.WorkoutsClaims;
import com.liamo.workouts.auth.model.dto.CreateUserRequestDTO;
import com.liamo.workouts.auth.model.entity.UserInfo;
import com.liamo.workouts.auth.model.entity.UserVerification;
import com.liamo.workouts.auth.repository.UserInfoRepository;
import com.liamo.workouts.auth.repository.UserVerificationRepository;
import jakarta.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * A service to manage {@link UserInfo} entities.
 */
@Service
public class UserInfoService {
    private static final Logger logger = LoggerFactory.getLogger(UserInfoService.class);

    private final UserInfoRepository userInfoRepository;
    private final UserVerificationRepository userVerificationRepository;
    private final PasswordEncoder passwordEncoder;

    public UserInfoService(
        UserInfoRepository userInfoRepository,
        UserVerificationRepository userVerificationRepository,
        PasswordEncoder passwordEncoder
    ) {
        this.userInfoRepository = userInfoRepository;
        this.userVerificationRepository = userVerificationRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Find by primary key id
     */
    public Optional<UserInfo> findById(long id) {
        return userInfoRepository.findById(id);
    }

    /**
     * Find by the publicId UUID
     */
    @Transactional(readOnly = true)
    public Optional<UserInfo> findByPublicId(UUID publicId) {
        return userInfoRepository.findByPublicId(publicId);
    }

    /**
     * Find by {@link AuthProvider} and email ignoring case
     */
    @Transactional(readOnly = true)
    public Optional<UserInfo> findByProviderAndEmailIgnoreCase(AuthProvider provider, String email) {
        return userInfoRepository.findByProviderAndEmailIgnoreCase(provider, email);
    }

    /**
     * Update a {@link UserInfo}'s lastLogin.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int updateLastLoginByPublicId(UUID publicId) {
        return userInfoRepository.updateLastLoginByPublicId(publicId);
    }

    /**
     * Registers a new LOCAL user in our user management platform.
     *
     * @param request Information from the frontend about the new user
     * @return A {@link UserVerification} object to trigger the email verification process
     * @throws UserAlreadyExistsException If the user already exists by email
     */
    @Transactional
    public UserVerification registerNewLocalUser(CreateUserRequestDTO request)
        throws UserAlreadyExistsException {
        boolean userAlreadyExists =
            userInfoRepository.existsByProviderAndEmailIgnoreCase(
                AuthProvider.LOCAL,
                request.email()
            );
        if (userAlreadyExists) {
            throw new UserAlreadyExistsException(request.email());
        }

        UUID publicIdAndSub = UUID.randomUUID();
        UserInfo newUser = UserInfo
            .newBuilder()
            .provider(AuthProvider.LOCAL)
            .sub(publicIdAndSub.toString())
            .publicId(publicIdAndSub)
            .email(request.email())
            .givenName(request.givenName())
            .familyName(request.familyName())
            .passwordHash(passwordEncoder.encode(request.password()))
            .addRole(AuthRole.USER)
            .build();

        newUser = userInfoRepository.save(newUser);
        UserVerification userVerification = new UserVerification(newUser, 24);
        return userVerificationRepository.save(userVerification);
    }

    /**
     * Fetches an OAuth2 user if it exists, otherwise JIT registers the user as new.
     * If the user is not new, we always make sure our claims information is up to date
     * by syncing the information from the provider claims to our own UserInfo information.
     *
     * @param provider The OAuth2 provider
     * @param claims   Information about the user in case we need to register them
     * @return The found user or the newly created user
     */
    @Transactional
    public UserInfo findOrRegisterOAuthUser(AuthProvider provider, WorkoutsClaims claims) {
        UserInfo user;
        Optional<UserInfo> userInfo = userInfoRepository.findByProviderAndSub(provider, claims.sub());
        if (userInfo.isPresent()) {
            user = userInfo.get();
            // Sync claims
            user.setEmail(claims.email());
            user.setGivenName(claims.givenName());
            user.setFamilyName(claims.familyName());
        } else {
            UserInfo newUser = UserInfo
                .newBuilder()
                .provider(provider)
                .sub(claims.sub())
                .email(claims.email())
                .givenName(claims.givenName())
                .familyName(claims.familyName())
                .addRole(AuthRole.USER)
                .emailVerified(true)
                .build();
            user = userInfoRepository.save(newUser);
        }

        return user;
    }

    /**
     * Verifies a LOCAL user's email.
     *
     * @param tokenString The token related to the UserVerification
     * @return A boolean if the verification was successful or not
     */
    // TODO: This logic and repo should be moved into a cache instead
    @Transactional
    public boolean verifyUserEmail(@Nonnull String tokenString) {
        UUID token;
        try {
            token = UUID.fromString(tokenString);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid token string: {}", tokenString);
            return false;
        }

        // We load the user into the Hibernate context from here, and from here
        // hibernate tracks changes to the UserInfo object
        Optional<UserVerification> userVerification = userVerificationRepository.findByToken(token);
        if (userVerification.isEmpty()) {
            logger.warn("Token not found: {}", token);
            return false;
        }

        UserVerification verification = userVerification.get();
        if (verification.getExpiration().isBefore(Instant.now())) {
            logger.warn("Token expired: {}", verification.getToken());
            return false;
        }

        // Since Hibernate tracks the UserInfo object, we can just update its info,
        // and it will automatically persist the data changes to the db
        UserInfo userInfo = verification.getUserInfo();
        userInfo.setEmailVerified(true);

        // Delete row after validating
        userVerificationRepository.delete(verification);

        logger.debug("User(id={}) email verified", userInfo.getId());
        return true;
    }

    /**
     * Creates a new {@link UserVerification} object to verify a LOCAL user's email.
     *
     * @param email The LOCAL user's email
     * @return An Optional containing the {@link UserVerification} object if we were able
     * to create it, otherwise, empty
     */
    @Transactional
    public Optional<UserVerification> createNewUserVerification(String email) {
        try {
            UserInfo userInfo = userInfoRepository.findByProviderAndEmailIgnoreCase(
                AuthProvider.LOCAL,
                email
            ).orElseThrow(() -> new UsernameNotFoundException(email));
            if (userInfo.isEmailVerified()) {
                logger.warn("User(email={}) email is already verified", email);
                return Optional.empty();
            }

            UserVerification verification = new UserVerification(userInfo, 24);
            return Optional.of(userVerificationRepository.save(verification));
        } catch (UsernameNotFoundException e) {
            logger.warn("User(email={}) unable to find by email", email);
            return Optional.empty();
        }
    }
}
