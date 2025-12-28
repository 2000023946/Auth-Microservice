package com.authservice.domain.model.aggregates.mfaToken;

import com.authservice.domain.model.valueobjects.MFACodeValueObject;
import com.authservice.domain.model.valueobjects.UUIDValueObject;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Factory responsible for the creation and reconstitution of
 * MFATokenAggregates.
 * <p>
 * This factory ensures that MFA codes and identities are correctly parsed from
 * raw strings before the domain logic is executed.
 */
public class MFATokenFactory {

    /**
     * Creates a brand new MFAToken for a user challenge.
     *
     * @param rawCode   The plain text MFA code (e.g., "123456").
     * @param expiresAt The expiration timestamp.
     * @param rawUserId The raw UUID string of the user.
     * @return A fully initialized MFATokenAggregate.
     */
    public MFATokenAggregate create(int rawCode, LocalDateTime expiresAt, String rawUserId) {
        // Validation happens during Value Object instantiation
        MFACodeValueObject codeVO = new MFACodeValueObject(rawCode);
        UUIDValueObject userIdVO = new UUIDValueObject(rawUserId);

        // Generate a fresh identity for this specific token record
        UUIDValueObject tokenIdVO = new UUIDValueObject(UUID.randomUUID().toString());

        return new MFATokenAggregate(tokenIdVO, codeVO, expiresAt, userIdVO);
    }

    /**
     * Reconstitutes an MFAToken from raw database state.
     * <p>
     * Parses raw strings from persistence into structured Domain objects.
     *
     * @param rawTokenId   The UUID string from storage.
     * @param rawCode      The stored MFA code string.
     * @param rawIssuedAt  The ISO-8601 string of token creation.
     * @param rawExpiresAt The ISO-8601 string of token expiration.
     * @param isRevoked    The revocation status.
     * @param rawUserId    The UUID string of the associated user.
     * @return A reconstituted MFATokenAggregate.
     * @throws java.time.format.DateTimeParseException if date strings are invalid.
     */
    public MFATokenAggregate reconstitute(
            String rawTokenId,
            int rawCode,
            String rawIssuedAt,
            String rawExpiresAt,
            boolean isRevoked,
            String rawUserId) {

        // 1. Convert raw strings to Value Objects
        UUIDValueObject tokenIdVO = new UUIDValueObject(rawTokenId);
        UUIDValueObject userIdVO = new UUIDValueObject(rawUserId);
        MFACodeValueObject codeVO = new MFACodeValueObject(rawCode);

        // 2. Parse ISO Strings to Domain Date Types (Fails fast if DB data is corrupt)
        LocalDateTime issuedAt = LocalDateTime.parse(rawIssuedAt);
        LocalDateTime expiresAt = LocalDateTime.parse(rawExpiresAt);

        // 3. Rebuild the Aggregate (Package-private access)
        return new MFATokenAggregate(
                tokenIdVO,
                codeVO,
                issuedAt,
                expiresAt,
                isRevoked,
                userIdVO);
    }
}