package com.authservice.domain.model.aggregates.accessToken;

import com.authservice.domain.model.aggregates.Token;
import com.authservice.domain.model.valueobjects.UUIDValueObject;
import java.time.LocalDateTime;

/**
 * Specialized Token Aggregate used for authorizing API requests.
 * Linked to a specific SessionId to ensure the token is valid only for the
 * active session.
 * *
 * <p>
 * Matches the 'AccessToken' table in the 3NF schema, providing the
 * link between a base Token and a specific Session context.
 * </p>
 */
public class AccessTokenAggregate extends Token<String> {

    /** The unique identifier of the session this access token belongs to. */
    private final UUIDValueObject sessionId;

    /**
     * Constructor for creating a BRAND NEW AccessToken.
     * Use this when a user logs in or refreshes their session.
     *
     * @param tokenId   Unique identifier for the token metadata
     * @param value     The JWT or secure string value of the access token
     * @param expiresAt The timestamp when this token becomes invalid (usually
     *                  short-lived)
     * @param sessionId The ID of the session this token is pinned to
     */
    AccessTokenAggregate(UUIDValueObject tokenId, String value, LocalDateTime expiresAt,
            UUIDValueObject sessionId) {
        super(tokenId, value, expiresAt);
        this.sessionId = sessionId;
    }

    /**
     * Constructor for RECONSTITUTING an AccessToken from the database.
     * Used by Repository Adapters to load the historical state of a token.
     *
     * @param tokenId   Existing identifier from the SQL Token table
     * @param value     The token value stored in the database
     * @param issuedAt  The original timestamp of token issuance
     * @param expiresAt The original expiration timestamp
     * @param isRevoked The current revocation status of the token
     * @param sessionId The ID of the session associated with this token
     */
    AccessTokenAggregate(UUIDValueObject tokenId, String value, LocalDateTime issuedAt,
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