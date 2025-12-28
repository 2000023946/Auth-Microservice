package com.authservice.domain.ports;

/**
 * Test bridge for {@link UserRepositoryProof}.
 * <p>
 * This class resides in {@code com.authservice.domain.ports} to access the
 * package-private constructor of {@link UserRepositoryProof} for testing
 * purposes.
 * It allows test code to mint valid proofs without exposing the constructor
 * publicly in production code.
 * </p>
 */
public class UserRepositoryProofTestBridge {

    /**
     * Creates a {@link UserRepositoryProof} instance for testing reconstitution
     * logic.
     * <p>
     * This method is intended for use in tests to simulate a proof coming from the
     * persistence layer, enabling the rehydration of a {@code UserAggregate}.
     * </p>
     *
     * @param rawUserId      the raw UUID of the user
     * @param rawEmail       the raw email address of the user
     * @param rawHash        the hashed password of the user
     * @param verified       whether the user has been verified
     * @param failedAttempts the number of failed login attempts
     * @param rawCreated     the creation timestamp as a raw string
     * @param rawUpdated     the last update timestamp as a raw string
     * @param rawLastReset   the last password reset timestamp as a raw string
     * @return a new {@link UserRepositoryProof} instance for testing
     */
    public static UserRepositoryProof create(
            String rawUserId, String rawEmail, String rawHash, boolean verified,
            int failedAttempts, String rawCreated, String rawUpdated, String rawLastReset) {

        return new UserRepositoryProof(
                rawUserId, rawEmail, rawHash, verified, failedAttempts,
                rawCreated, rawUpdated, rawLastReset);
    }
}
