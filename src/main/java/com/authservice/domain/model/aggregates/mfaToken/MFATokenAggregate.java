package com.authservice.domain.model.aggregates.mfaToken;

import com.authservice.domain.model.aggregates.Token;
import com.authservice.domain.model.valueobjects.MFACodeValueObject;
import com.authservice.domain.model.valueobjects.UUIDValueObject;
import java.time.LocalDateTime;

/**
 * Specialized Token Aggregate used for Multi-Factor Authentication.
 * Encapsulates a structured MFA code (e.g., TOTP or SMS code) pinned to a
 * specific User.
 * *
 * <p>
 * Matches the 'MFAToken' table in the 3NF schema, linking the base Token
 * metadata to a specific User and a validated MFA value.
 * </p>
 */
public class MFATokenAggregate extends Token<MFACodeValueObject> {

    /** The unique identifier of the user who must verify this MFA code. */
    private final UUIDValueObject userId;

    /**
     * Constructor for creating a BRAND NEW MFAToken.
     * Use this when a user successfully provides credentials and triggers the MFA
     * flow.
     *
     * @param tokenId   Unique identifier for the token metadata
     * @param value     The MFAValueObject containing the code and its validation
     *                  logic
     * @param expiresAt The timestamp when this MFA attempt expires (usually very
     *                  short-lived)
     * @param userId    The ID of the user undergoing MFA
     */
    MFATokenAggregate(UUIDValueObject tokenId, MFACodeValueObject value, LocalDateTime expiresAt,
            UUIDValueObject userId) {
        super(tokenId, value, expiresAt);
        this.userId = userId;
    }

    /**
     * Constructor for RECONSTITUTING an MFAToken from the database.
     * Used by Repository Adapters to load the state of a pending MFA challenge.
     *
     * @param tokenId   Existing identifier from the SQL Token table
     * @param value     The MFAValueObject reconstituted from the database string
     * @param issuedAt  The original timestamp of the MFA request
     * @param expiresAt The original expiration timestamp
     * @param isRevoked The current revocation status
     * @param userId    The ID of the user associated with this MFA attempt
     */
    MFATokenAggregate(UUIDValueObject tokenId, MFACodeValueObject value, LocalDateTime issuedAt,
            LocalDateTime expiresAt, boolean isRevoked, UUIDValueObject userId) {
        super(tokenId, value, issuedAt, expiresAt, isRevoked);
        this.userId = userId;
    }

    /**
     * Verifies if this MFA token is valid for a specific user.
     * * @param userIdToMatch the user ID provided during the MFA verification step
     * 
     * @return true if the token is active and belongs to the specified user
     */
    @Override
    public boolean isValidFor(UUIDValueObject userIdToMatch) {
        return this.isActive() && this.userId.equals(userIdToMatch);
    }

    /**
     * Gets the associated UserId for this MFA token.
     * 
     * @return the UUIDValueObject of the user
     */
    public UUIDValueObject getUserId() {
        return userId;
    }
}