package com.authservice.domain.model.aggregates.accessToken;

import com.authservice.domain.model.valueobjects.UUIDValueObject;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Factory responsible for creating and reconstituting AccessTokenAggregates.
 * <p>
 * This factory translates raw input types and database strings into the
 * structured domain types required for Access Token management.
 */
public class AccessTokenFactory {

    /**
     * Creates a brand new AccessToken.
     * Use this when issuing a new JWT after a successful login or refresh.
     *
     * @param rawValue     The JWT or secure token string.
     * @param expiresAt    The calculated expiration time.
     * @param rawSessionId The raw UUID string of the session this token is pinned
     *                     to.
     * @return A fully initialized AccessTokenAggregate.
     */
    public AccessTokenAggregate create(String rawValue, LocalDateTime expiresAt, String rawSessionId) {
        // Validation of ID and Session ID happens here
        UUIDValueObject tokenIdVO = new UUIDValueObject(UUID.randomUUID().toString());
        UUIDValueObject sessionIdVO = new UUIDValueObject(rawSessionId);

        return new AccessTokenAggregate(tokenIdVO, rawValue, expiresAt, sessionIdVO);
    }

    /**
     * Reconstitutes an AccessToken from raw database state.
     * <p>
     * Follows the "DB returns str" rule: parses raw strings from the
     * persistence layer into Domain objects.
     *
     * @param rawTokenId   The UUID string from the Token table.
     * @param value        The actual token string (JWT).
     * @param rawIssuedAt  The ISO-8601 string of token creation.
     * @param rawExpiresAt The ISO-8601 string of token expiration.
     * @param isRevoked    The revocation status.
     * @param rawSessionId The UUID string of the associated session.
     * @return A reconstituted AccessTokenAggregate.
     * @throws java.time.format.DateTimeParseException if date strings are
     *                                                 malformed.
     */
    public AccessTokenAggregate reconstitute(
            String rawTokenId,
            String value,
            String rawIssuedAt,
            String rawExpiresAt,
            boolean isRevoked,
            String rawSessionId) {

        // 1. Convert raw ID strings to Value Objects
        UUIDValueObject tokenIdVO = new UUIDValueObject(rawTokenId);
        UUIDValueObject sessionIdVO = new UUIDValueObject(rawSessionId);

        // 2. Parse ISO Strings to Domain Date Types (Fails fast on invalid strings)
        LocalDateTime issuedAt = LocalDateTime.parse(rawIssuedAt);
        LocalDateTime expiresAt = LocalDateTime.parse(rawExpiresAt);

        // 3. Rebuild the Aggregate using the package-private constructor
        return new AccessTokenAggregate(
                tokenIdVO,
                value,
                issuedAt,
                expiresAt,
                isRevoked,
                sessionIdVO);
    }
}