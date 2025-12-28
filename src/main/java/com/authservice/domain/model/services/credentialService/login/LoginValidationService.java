package com.authservice.domain.model.services.credentialService.login;

import com.authservice.domain.model.valueobjects.EmailValueObject;
import com.authservice.domain.ports.IHashingService;
import com.authservice.domain.ports.IUserRepository;
import com.authservice.domain.model.aggregates.user.UserAggregate;
import com.authservice.domain.model.services.credentialService.CredentialValidationService;

import java.util.Optional;

/**
 * Specialized domain service for authenticating existing users.
 * <p>
 * This service implements a multi-stage validation process: structural checks,
 * identity lookup, account status verification, and cryptographic hash
 * matching.
 * </p>
 */
public class LoginValidationService extends CredentialValidationService {

    /** Service used for verifying cryptographic password hashes. */
    private final IHashingService hashingService;

    /**
     * Constructs a new LoginValidationService.
     *
     * @param userRepository the repository port used to fetch user data
     * @param hashingService the service port used for password verification
     */
    public LoginValidationService(IUserRepository userRepository, IHashingService hashingService) {
        super(userRepository);
        this.hashingService = hashingService;
    }

    /**
     * Orchestrates the full authentication validation pipeline.
     *
     * @param email    the raw email string
     * @param password the raw password string
     * @return an {@link AuthProof} containing the user and the result
     */
    public AuthProof validateForLogin(String email, String password) {
        validate(email, password);

        Optional<UserAggregate> userOpt = findUser(email);

        // Case: User not found (user is null in the proof)
        if (userOpt.isEmpty()) {
            return new FailedAuthProof(null, AuthFailureReason.INVALID_CREDENTIALS);
        }

        UserAggregate user = userOpt.get();

        if (isAccountLocked(user)) {
            return new FailedAuthProof(user, AuthFailureReason.ACCOUNT_LOCKED);
        }

        return verifyCredentials(user, password);
    }

    /**
     * Normalizes input and attempts to retrieve the user identity from the
     * repository.
     *
     * @param email the raw email string to be converted into a Value Object
     * @return an Optional containing the UserAggregate if found, otherwise empty
     */
    private Optional<UserAggregate> findUser(String email) {
        EmailValueObject emailVO = new EmailValueObject(email);
        return userRepository.findByEmail(emailVO);
    }

    /**
     * Evaluates if the user account is currently in a locked state.
     *
     * @param user the UserAggregate instance to evaluate
     * @return true if the account is locked; false otherwise
     */
    private boolean isAccountLocked(UserAggregate user) {
        return user.isLocked();
    }

    /**
     * Performs the final cryptographic comparison between the raw password and
     * stored hash.
     *
     * @param user     the UserAggregate containing the stored password hash
     * @param password the raw password string to verify
     * @return a {@link SuccessfulAuthProof} on match, or a {@link FailedAuthProof}
     *         on mismatch
     */
    private AuthProof verifyCredentials(UserAggregate user, String password) {
        boolean matches = hashingService.verify(password, user.getPasswordHash().getValue());

        return matches
                ? new SuccessfulAuthProof(user)
                : new FailedAuthProof(user, AuthFailureReason.INVALID_CREDENTIALS);
    }
}