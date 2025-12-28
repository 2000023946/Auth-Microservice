package com.authservice.domain.model.aggregates;

import com.authservice.domain.model.valueobjects.UUIDValueObject;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Abstract Base Entity for all Tokens in the system.
 * Matches the base 'Token' table in the 3NF schema.
 *
 * @param <T> The type of the actual token value (e.g., String, JWT)
 */
public abstract class Token<T> {
    private final UUIDValueObject tokenId;
    private final T value;
    private final LocalDateTime issuedAt;
    private final LocalDateTime expiresAt;
    private boolean isRevoked;

    /**
     * Constructor for creating a BRAND NEW token.
     * Automatically sets issuedAt to now and isRevoked to false.
     *
     * @param tokenId   Unique identifier for the token
     * @param value     The actual generic value of the token
     * @param expiresAt The timestamp when the token is no longer valid
     */
    protected Token(UUIDValueObject tokenId, T value, LocalDateTime expiresAt) {
        this.tokenId = Objects.requireNonNull(tokenId, "TokenId cannot be null");
        this.value = Objects.requireNonNull(value, "Token value cannot be null");
        this.issuedAt = LocalDateTime.now();
        this.expiresAt = Objects.requireNonNull(expiresAt, "Expiration time cannot be null");
        this.isRevoked = false;
    }

    /**
     * Constructor for RECONSTITUTING a token from the database.
     * Used by Repository Adapters to load existing state.
     *
     * @param tokenId   Existing identifier from SQL
     * @param value     Existing value from SQL
     * @param issuedAt  The original issuance timestamp
     * @param expiresAt The original expiration timestamp
     * @param isRevoked The current revocation status
     */
    protected Token(UUIDValueObject tokenId, T value, LocalDateTime issuedAt,
            LocalDateTime expiresAt, boolean isRevoked) {
        this.tokenId = Objects.requireNonNull(tokenId, "TokenId cannot be null");
        this.value = Objects.requireNonNull(value, "Token value cannot be null");
        this.issuedAt = Objects.requireNonNull(issuedAt, "IssuedAt cannot be null");
        this.expiresAt = Objects.requireNonNull(expiresAt, "Expiration time cannot be null");
        this.isRevoked = isRevoked;
    }

    /**
     * Abstract requirement for all tokens to verify ownership.
     * 
     * @param ownerId The UUID of the owner (SessionId or UserId) to verify
     *                against.
     * 
     * @return true if the token is active and belongs to the provided ID.
     */
    public abstract boolean isValidFor(UUIDValueObject ownerId);

    /**
     * Determines if the token is currently active.
     * A token is active if it is not revoked and not yet expired.
     *
     * @return true if the token can still be used
     */
    public boolean isActive() {
        return !isRevoked && LocalDateTime.now().isBefore(expiresAt);
    }

    /**
     * Manually revokes the token, rendering it inactive immediately.
     *
     * @throws IllegalCallerException if the token is already revoked
     */
    public void revoke() {
        if (this.isRevoked) {
            throw new IllegalCallerException("Cannot revoke any revoked tokens");
        }
        this.isRevoked = true;
    }

    /**
     * Gets the unique identifier of the token.
     * 
     * @return the UUIDValueObject of the token
     */
    public UUIDValueObject getTokenId() {
        return tokenId;
    }

    /**
     * Gets the generic value of the token.
     * 
     * @return the token value of type T
     */
    public T getValue() {
        return value;
    }

    /**
     * Gets the issuance timestamp.
     * 
     * @return LocalDateTime of issuance
     */
    public LocalDateTime getIssuedAt() {
        return issuedAt;
    }

    /**
     * Gets the expiration timestamp.
     * 
     * @return LocalDateTime of expiration
     */
    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    /**
     * Checks the revocation status.
     * 
     * @return true if the token was manually revoked
     */
    public boolean isRevoked() {
        return isRevoked;
    }
}