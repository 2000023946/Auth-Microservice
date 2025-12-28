package com.authservice.domain.ports;

/**
 * Proof of authenticity for a User entity retrieved from the persistence layer.
 * <p>
 * This class encapsulates the raw state of a User and serves as the exclusive
 * "key" required to rehydrate a
 * {@link com.authservice.domain.aggregates.UserAggregate}.
 * It is designed to be immutable and package-private construction ensures
 * that only authorized infrastructure or persistence components can create it.
 * </p>
 */
public final class UserRepositoryProof {

    private final String rawUserId;
    private final String rawEmail;
    private final String rawHash;
    private final boolean verified;
    private final int failedAttempts;
    private final String rawCreated;
    private final String rawUpdated;
    private final String rawLastReset;

    /**
     * Constructs a new {@code UserRepositoryProof} instance.
     * <p>
     * This constructor is package-private to restrict creation to the
     * infrastructure
     * layer, ensuring that only trusted components can produce valid proofs.
     * </p>
     *
     * @param rawUserId      the raw UUID of the user
     * @param rawEmail       the raw email address of the user
     * @param rawHash        the hashed password of the user
     * @param verified       whether the user has been verified
     * @param failedAttempts the number of failed login attempts
     * @param rawCreated     the timestamp of user creation in raw string format
     * @param rawUpdated     the timestamp of last update in raw string format
     * @param rawLastReset   the timestamp of the last password reset in raw string
     *                       format
     */
    UserRepositoryProof(
            String rawUserId,
            String rawEmail,
            String rawHash,
            boolean verified,
            int failedAttempts,
            String rawCreated,
            String rawUpdated,
            String rawLastReset) {
        this.rawUserId = rawUserId;
        this.rawEmail = rawEmail;
        this.rawHash = rawHash;
        this.verified = verified;
        this.failedAttempts = failedAttempts;
        this.rawCreated = rawCreated;
        this.rawUpdated = rawUpdated;
        this.rawLastReset = rawLastReset;
    }

    /**
     * Returns the raw user ID.
     *
     * @return the raw user ID string
     */
    public String getRawUserId() {
        return rawUserId;
    }

    /**
     * Returns the raw email of the user.
     *
     * @return the raw email string
     */
    public String getRawEmail() {
        return rawEmail;
    }

    /**
     * Returns the hashed password.
     *
     * @return the raw password hash
     */
    public String getRawHash() {
        return rawHash;
    }

    /**
     * Indicates whether the user has been verified.
     *
     * @return {@code true} if verified; {@code false} otherwise
     */
    public boolean isVerified() {
        return verified;
    }

    /**
     * Returns the number of failed login attempts.
     *
     * @return the number of failed attempts
     */
    public int getFailedAttempts() {
        return failedAttempts;
    }

    /**
     * Returns the raw creation timestamp.
     *
     * @return creation timestamp as a raw string
     */
    public String getRawCreated() {
        return rawCreated;
    }

    /**
     * Returns the raw timestamp of the last update.
     *
     * @return last update timestamp as a raw string
     */
    public String getRawUpdated() {
        return rawUpdated;
    }

    /**
     * Returns the raw timestamp of the last password reset.
     *
     * @return last reset timestamp as a raw string
     */
    public String getRawLastReset() {
        return rawLastReset;
    }
}
