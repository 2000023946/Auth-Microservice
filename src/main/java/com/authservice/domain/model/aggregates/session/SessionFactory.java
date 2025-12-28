package com.authservice.domain.model.aggregates.session;

import com.authservice.domain.model.valueobjects.UUIDValueObject;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Factory responsible for creating and reconstituting SessionAggregates.
 * <p>
 * This factory acts as the translation layer between raw persistence formats
 * (like JSON strings) and the rich Domain Model.
 */
public class SessionFactory {

    /**
     * Creates a brand new SessionAggregate for a fresh login.
     *
     * @param rawUserId The raw User ID string.
     * @param expiresAt The timestamp when this session should expire.
     * @return A new SessionAggregate instance.
     */
    public static SessionAggregate create(String rawUserId, LocalDateTime expiresAt) {
        UUIDValueObject userIdVO = new UUIDValueObject(rawUserId);
        UUIDValueObject sessionIdVO = new UUIDValueObject(UUID.randomUUID().toString());

        return new SessionAggregate(sessionIdVO, userIdVO, expiresAt);
    }

    /**
     * Reconstitutes an existing SessionAggregate from raw database strings.
     * <p>
     * Use this when the database returns a JSON object where dates and IDs are
     * strings.
     *
     * @param rawSessionId    The session UUID string.
     * @param rawUserId       The user UUID string.
     * @param rawCreatedAt    The ISO-8601 creation timestamp string.
     * @param rawLastActivity The ISO-8601 last activity timestamp string.
     * @param rawExpiresAt    The ISO-8601 expiration timestamp string.
     * @param isRevoked       The revocation status.
     * @return A reconstituted SessionAggregate.
     */
    public static SessionAggregate reconstitute(
            String rawSessionId,
            String rawUserId,
            String rawCreatedAt,
            String rawLastActivity,
            String rawExpiresAt,
            boolean isRevoked) {

        // 1. Map ID Strings to Value Objects
        UUIDValueObject sessionIdVO = new UUIDValueObject(rawSessionId);
        UUIDValueObject userIdVO = new UUIDValueObject(rawUserId);

        // 2. Parse Date Strings to LocalDateTime
        // Assumes standard ISO_LOCAL_DATE_TIME format (e.g., "2025-12-27T13:45:00")
        LocalDateTime createdAt = LocalDateTime.parse(rawCreatedAt);
        LocalDateTime lastActivityAt = LocalDateTime.parse(rawLastActivity);
        LocalDateTime expiresAt = LocalDateTime.parse(rawExpiresAt);

        // 3. Rebuild the Aggregate using the package-private constructor
        return new SessionAggregate(
                sessionIdVO,
                userIdVO,
                createdAt,
                lastActivityAt,
                expiresAt,
                isRevoked);
    }
}