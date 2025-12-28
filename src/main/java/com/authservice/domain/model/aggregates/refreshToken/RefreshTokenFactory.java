package com.authservice.domain.model.aggregates.refreshToken;

import com.authservice.domain.model.valueobjects.UUIDValueObject;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Factory responsible for creating and reconstituting RefreshTokenAggregates.
 * <p>
 * This acts as the translation layer between the Infrastructure (DB Strings)
 * and the Domain (Value Objects/Aggregates).
 */
public class RefreshTokenFactory {

    /**
     * Creates a brand new RefreshToken.
     * Use this during login or when rotating tokens.
     *
     * @param rawValue     The secure string value of the token.
     * @param expiresAt    The calculated expiration timestamp.
     * @param rawSessionId The raw UUID string of the session.
     * @return A fully initialized RefreshTokenAggregate.
     */
    public RefreshTokenAggregate create(String rawValue, LocalDateTime expiresAt, String rawSessionId) {
        // Validation of ID strings happens during VO instantiation
        UUIDValueObject tokenIdVO = new UUIDValueObject(UUID.randomUUID().toString());
        UUIDValueObject sessionIdVO = new UUIDValueObject(rawSessionId);

        return new RefreshTokenAggregate(tokenIdVO, rawValue, expiresAt, sessionIdVO);
    }

    /**
     * Reconstitutes an existing RefreshToken from database state.
     * <p>
     * Implementation follows the "DB returns str" rule: parses raw strings
     * from persistence into Domain types.
     *
     * @param rawTokenId   The UUID string from storage.
     * @param value        The stored token value.
     * @param rawIssuedAt  The ISO-8601 issuance timestamp string.
     * @param rawExpiresAt The ISO-8601 expiration timestamp string.
     * @param isRevoked    The current revocation status.
     * @param rawSessionId The UUID string of the associated session.
     * @return A reconstituted RefreshTokenAggregate.
     * @throws java.time.format.DateTimeParseException if date strings are
     *                                                 malformed.
     */
    public RefreshTokenAggregate reconstitute(
            String rawTokenId,
            String value,
            String rawIssuedAt,
            String rawExpiresAt,
            boolean isRevoked,
            String rawSessionId) {

        // 1. Map raw strings to Domain Value Objects
        UUIDValueObject tokenIdVO = new UUIDValueObject(rawTokenId);
        UUIDValueObject sessionIdVO = new UUIDValueObject(rawSessionId);

        // 2. Parse ISO Strings into Domain Date Types (Fails fast on bad DB data)
        LocalDateTime issuedAt = LocalDateTime.parse(rawIssuedAt);
        LocalDateTime expiresAt = LocalDateTime.parse(rawExpiresAt);

        // 3. Rebuild the Aggregate (Using package-private access)
        return new RefreshTokenAggregate(
                tokenIdVO,
                value,
                issuedAt,
                expiresAt,
                isRevoked,
                sessionIdVO);
    }
}