package com.authservice.domain.model.aggregates.refreshToken;

import com.authservice.domain.model.aggregates.Token;
import com.authservice.domain.model.valueobjects.UUIDValueObject;
import java.time.LocalDateTime;

/**
 * Specialized Token Aggregate used to refresh access sessions.
 * Linked to a specific SessionId to facilitate token rotation and session
 * management.
 * *
 * <p>
 * Matches the 'RefreshToken' table in the 3NF schema, providing the
 * link between a base Token and a specific Session.
 * </p>
 */
public class RefreshTokenAggregate extends Token<String> {

    /** The unique identifier of the session this refresh token is pinned to. */
    private final UUIDValueObject sessionId;

    /**
     * Constructor for creating a BRAND NEW RefreshToken.
     * Use this in the Login or Refresh Use Cases when issuing a new token.
     *
     * @param tokenId   Unique identifier for the token metadata
     * @param value     The secure string value of the refresh token
     * @param expiresAt The timestamp when this token becomes invalid
     * @param sessionId The ID of the session this token belongs to
     */
    RefreshTokenAggregate(UUIDValueObject tokenId, String value, LocalDateTime expiresAt,
            UUIDValueObject sessionId) {
        super(tokenId, value, expiresAt);
        this.sessionId = sessionId;
    }

    /**
     * Constructor for RECONSTITUTING a RefreshToken from the database.
     * Used by Repository Adapters to load the historical state of a token.
     *
     * @param tokenId   Existing identifier from the SQL Token table
     * @param value     The token value stored in the database
     * @param issuedAt  The original timestamp of token issuance
     * @param expiresAt The original expiration timestamp
     * @param isRevoked The current revocation status of the token
     * @param sessionId The ID of the session associated with this token
     */
    RefreshTokenAggregate(UUIDValueObject tokenId, String value, LocalDateTime issuedAt,
            LocalDateTime expiresAt, boolean isRevoked, UUIDValueObject sessionId) {
        super(tokenId, value, issuedAt, expiresAt, isRevoked);
        this.sessionId = sessionId;
    }

    /**
     * Implementation for Session-based tokens.
     * This sees if the associated token belongs to the
     * user's session
     * 
     * @param sessionIdToMatch the current session of the user
     * 
     */
    @Override
    public boolean isValidFor(UUIDValueObject sessionIdToMatch) {
        return this.isActive() && this.sessionId.equals(sessionIdToMatch);
    }

    /**
     * Gets the associated SessionId for this token.
     * 
     * @return the UUIDValueObject representing the session owner
     */
    public UUIDValueObject getSessionId() {
        return sessionId;
    }
}