
package com.authservice.domain.model.aggregates.session;

import com.authservice.domain.model.valueobjects.UUIDValueObject;
import java.time.LocalDateTime;

/**
 * Aggregate Root representing a user's active authentication session.
 * Encapsulates the lifecycle, activity tracking, and revocation logic for a
 * session.
 */
public class SessionAggregate {

    /** The unique identifier for the session. */
    private final UUIDValueObject sessionId;

    /** The unique identifier of the user who owns this session. */
    private final UUIDValueObject userId;

    /** The timestamp when the session was originally created. */
    private final LocalDateTime createdAt;

    /** The timestamp of the most recent activity recorded for this session. */
    private LocalDateTime lastActivityAt;

    /** The timestamp when the session is scheduled to expire. */
    private LocalDateTime expiresAt;

    /** Flag indicating if the session has been explicitly revoked. */
    private boolean isRevoked;

    /**
     * Constructor for creating a BRAND NEW SessionAggregate.
     * Use this during the initial login process after successful authentication.
     *
     * @param sessionId The unique identifier generated for this session
     * @param userId    The ID of the user owning the session
     * @param expiresAt The initial expiration timestamp for the session
     */
    SessionAggregate(UUIDValueObject sessionId, UUIDValueObject userId, LocalDateTime expiresAt) {
        this.sessionId = sessionId;
        this.userId = userId;
        this.createdAt = LocalDateTime.now();
        this.lastActivityAt = LocalDateTime.now();
        this.expiresAt = expiresAt;
        this.isRevoked = false;
    }

    /**
     * Constructor for RECONSTITUTING a SessionAggregate from the database.
     * Used by Repository Adapters to rebuild the session from SQL state.
     *
     * @param sessionId      Existing session identifier
     * @param userId         Existing user identifier
     * @param createdAt      Original creation timestamp
     * @param lastActivityAt Timestamp of the last recorded activity
     * @param expiresAt      The current expiration timestamp
     * @param isRevoked      Current revocation status
     */
    SessionAggregate(UUIDValueObject sessionId,
            UUIDValueObject userId,
            LocalDateTime createdAt,
            LocalDateTime lastActivityAt,
            LocalDateTime expiresAt,
            boolean isRevoked) {
        this.sessionId = sessionId;
        this.userId = userId;
        this.createdAt = createdAt;
        this.lastActivityAt = lastActivityAt;
        this.expiresAt = expiresAt;
        this.isRevoked = isRevoked;
    }

    /**
     * Deactivates the session immediately.
     * Once deactivated, the session cannot be reactivated.
     */
    public void deactivate() {
        if (this.isRevoked) {
            throw new IllegalCallerException("Cannot deactive a session that is already deactived!");
        }
        this.isRevoked = true;
    }

    /**
     * Checks if the session is currently valid and usable.
     * A session is active if it is not revoked and the current time is before
     * expiration.
     *
     * @return true if the session is active, false otherwise
     */
    public boolean isActive() {
        return !isRevoked && LocalDateTime.now().isBefore(expiresAt);
    }

    /**
     * Updates the last activity timestamp to the current time.
     * This is typically called whenever an access token is successfully verified or
     * refreshed.
     */
    public void updateLastActivity() {
        this.lastActivityAt = LocalDateTime.now();
    }

    /**
     * Gets the unique session identifier.
     * 
     * @return the sessionId value object
     */
    public UUIDValueObject getSessionId() {
        return sessionId;
    }

    /**
     * Gets the user identifier associated with this session.
     * 
     * @return the userId value object
     */
    public UUIDValueObject getUserId() {
        return userId;
    }

    /**
     * Gets the creation timestamp.
     * 
     * @return the LocalDateTime of creation
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Gets the last activity timestamp.
     * 
     * @return the LocalDateTime of last activity
     */
    public LocalDateTime getLastActivityAt() {
        return lastActivityAt;
    }

    /**
     * Gets the expiration timestamp.
     * 
     * @return the LocalDateTime of expiration
     */
    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    /**
     * Checks the revocation status.
     * 
     * @return true if the session has been revoked
     */
    public boolean isRevoked() {
        return isRevoked;
    }
}