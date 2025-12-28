package com.authservice.domain.model.aggregates.passswordResetToken;

import com.authservice.domain.model.valueobjects.UUIDValueObject;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Factory responsible for the creation and reconstitution of
 * PasswordResetTokenAggregates.
 * <p>
 * Ensures that all identifiers and timestamps are correctly parsed and
 * validated
 * before the aggregate enters the Domain Layer.
 */
public class PasswordResetTokenFactory {

    /**
     * Creates a brand new PasswordResetToken.
     * Use this when a user initiates the "Forgot Password" process.
     *
     * @param rawValue  The secure random string generated for the reset link.
     * @param expiresAt The timestamp when the reset window expires.
     * @param rawUserId The raw UUID string of the user requesting the reset.
     * @return A fully initialized PasswordResetTokenAggregate.
     */
    public PasswordResetTokenAggregate create(String rawValue, LocalDateTime expiresAt, String rawUserId) {
        // Validation of ID strings happens during VO instantiation
        UUIDValueObject tokenIdVO = new UUIDValueObject(UUID.randomUUID().toString());
        UUIDValueObject userIdVO = new UUIDValueObject(rawUserId);

        return new PasswordResetTokenAggregate(tokenIdVO, rawValue, expiresAt, userIdVO);
    }

    /**
     * Reconstitutes a PasswordResetToken from raw database state.
     * <p>
     * Implementation follows the "DB returns str" rule, parsing all primitive
     * types into Domain types.
     *
     * @param rawTokenId   The UUID string from the Token table.
     * @param value        The stored token value/hash.
     * @param rawIssuedAt  The ISO-8601 string of token issuance.
     * @param rawExpiresAt The ISO-8601 string of token expiration.
     * @param isRevoked    The current revocation status.
     * @param rawUserId    The UUID string of the associated user.
     * @return A reconstituted PasswordResetTokenAggregate.
     * @throws java.time.format.DateTimeParseException if date strings are
     *                                                 malformed.
     */
    public PasswordResetTokenAggregate reconstitute(
            String rawTokenId,
            String value,
            String rawIssuedAt,
            String rawExpiresAt,
            boolean isRevoked,
            String rawUserId) {

        // 1. Convert raw strings to Value Objects
        UUIDValueObject tokenIdVO = new UUIDValueObject(rawTokenId);
        UUIDValueObject userIdVO = new UUIDValueObject(rawUserId);

        // 2. Parse ISO Strings to Domain Date Types (Fails fast on invalid storage
        // data)
        LocalDateTime issuedAt = LocalDateTime.parse(rawIssuedAt);
        LocalDateTime expiresAt = LocalDateTime.parse(rawExpiresAt);

        // 3. Rebuild the Aggregate using package-private access
        return new PasswordResetTokenAggregate(
                tokenIdVO,
                value,
                issuedAt,
                expiresAt,
                isRevoked,
                userIdVO);
    }
}