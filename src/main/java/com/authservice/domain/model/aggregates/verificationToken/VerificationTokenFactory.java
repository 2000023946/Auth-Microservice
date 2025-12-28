package com.authservice.domain.model.aggregates.verificationToken;

import com.authservice.domain.model.valueobjects.UUIDValueObject;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Factory responsible for the creation and reconstitution of
 * VerificationTokenAggregates.
 * <p>
 * This factory ensures that identities and timestamps are correctly transformed
 * from raw infrastructure types into Domain-ready objects.
 */
public class VerificationTokenFactory {

    /**
     * Creates a brand new VerificationToken for a user registration flow.
     *
     * @param rawValue  The secure random string generated for the verification
     *                  link.
     * @param expiresAt The timestamp when the verification window expires.
     * @param rawUserId The raw UUID string of the user being verified.
     * @return A fully initialized VerificationTokenAggregate.
     */
    public VerificationTokenAggregate create(String rawValue, LocalDateTime expiresAt, String rawUserId) {
        // Validation of ID strings happens during VO instantiation
        UUIDValueObject tokenIdVO = new UUIDValueObject(UUID.randomUUID().toString());
        UUIDValueObject userIdVO = new UUIDValueObject(rawUserId);

        return new VerificationTokenAggregate(tokenIdVO, rawValue, expiresAt, userIdVO);
    }

    /**
     * Reconstitutes a VerificationToken from raw database state.
     * <p>
     * Follows the "DB returns str" rule: parses raw strings from the persistence
     * layer into structured Domain objects.
     *
     * @param rawTokenId   The UUID string from the Token table.
     * @param value        The verification string stored in the database.
     * @param rawIssuedAt  The ISO-8601 issuance timestamp string.
     * @param rawExpiresAt The ISO-8601 expiration timestamp string.
     * @param isRevoked    The current revocation status.
     * @param rawUserId    The UUID string of the associated user.
     * @return A reconstituted VerificationTokenAggregate.
     * @throws java.time.format.DateTimeParseException if date strings are
     *                                                 malformed.
     */
    public VerificationTokenAggregate reconstitute(
            String rawTokenId,
            String value,
            String rawIssuedAt,
            String rawExpiresAt,
            boolean isRevoked,
            String rawUserId) {

        // 1. Map raw strings to Domain Value Objects
        UUIDValueObject tokenIdVO = new UUIDValueObject(rawTokenId);
        UUIDValueObject userIdVO = new UUIDValueObject(rawUserId);

        // 2. Parse ISO Strings into Domain Date Types (Fails fast on bad DB data)
        LocalDateTime issuedAt = LocalDateTime.parse(rawIssuedAt);
        LocalDateTime expiresAt = LocalDateTime.parse(rawExpiresAt);

        // 3. Rebuild the Aggregate (Using package-private access)
        return new VerificationTokenAggregate(
                tokenIdVO,
                value,
                issuedAt,
                expiresAt,
                isRevoked,
                userIdVO);
    }
}