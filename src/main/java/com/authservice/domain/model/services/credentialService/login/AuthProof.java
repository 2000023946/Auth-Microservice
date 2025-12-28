package com.authservice.domain.model.services.credentialService.login;

import com.authservice.domain.model.aggregates.user.UserAggregate;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Base domain proof representing the result of a credential validation attempt.
 * <p>
 * An {@code AuthProof} captures when validation occurred and, if available,
 * which user aggregate was involved in the attempt.
 * </p>
 */
public abstract class AuthProof {

    /**
     * Time at which the credential validation was performed.
     */
    private final LocalDateTime timestamp;

    /**
     * User aggregate involved in the authentication attempt.
     * May be {@code null} if no user was found.
     */
    private final UserAggregate user;

    /**
     * Creates a new authentication proof.
     *
     * @param user the user aggregate involved in the attempt,
     *             or {@code null} if the user does not exist
     */
    AuthProof(UserAggregate user) {
        this.timestamp = LocalDateTime.now();
        this.user = user;
    }

    /**
     * Returns the time at which authentication was evaluated.
     *
     * @return authentication timestamp
     */
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    /**
     * Returns the user aggregate associated with this proof, if any.
     *
     * @return an {@link Optional} containing the user aggregate,
     *         or an empty Optional if no user was found
     */
    public Optional<UserAggregate> getUser() {
        return Optional.ofNullable(user);
    }
}
