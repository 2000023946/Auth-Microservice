package com.authservice.domain.model.aggregates.user;

import com.authservice.domain.model.aggregates.passswordResetToken.PasswordResetTokenAggregate;
import com.authservice.domain.model.aggregates.verificationToken.VerificationTokenAggregate;
import com.authservice.domain.model.services.credentialService.login.FailedAuthProof;
import com.authservice.domain.model.services.credentialService.login.SuccessfulAuthProof;
import com.authservice.domain.model.valueobjects.EmailValueObject;
import com.authservice.domain.model.valueobjects.PasswordHashValueObject;
import com.authservice.domain.model.valueobjects.UUIDValueObject;
import java.time.LocalDateTime;
import java.time.Duration;

/**
 * Aggregate Root representing a User in the system.
 * Handles identity lifecycle, security constraints, and token-based state
 * changes.
 */
public class UserAggregate {
    /** The unique identifier for the user. */
    private final UUIDValueObject userId;

    /** The user's validated email address. */
    private EmailValueObject email;

    /** The hashed version of the user's password. */
    private PasswordHashValueObject passwordHash;

    /** Flag indicating if the user's email has been verified. */
    private boolean verified;

    /** Counter for consecutive failed login attempts. */
    private int failedLoginAttempts;

    /** Timestamp of when the user account was created. */
    private final LocalDateTime createdAt;

    /** Timestamp of the last time the user state was modified. */
    private LocalDateTime updatedAt;

    /** Timestamp of the most recent password reset request. */
    private LocalDateTime lastPasswordResetRequestAt;

    /** Maximum number of failed attempts allowed before locking. */
    private static final int MAX_FAILED_ATTEMPTS = 5;

    /** Minimum duration required between password reset requests. */
    private static final int PASSWORD_RESET_COOLDOWN_MINUTES = 15;

    /**
     * Constructs a new UserAggregate with core identity data.
     *
     * @param userId       The unique identifier for the user
     * @param email        The validated email address
     * @param passwordHash The hashed password
     */
    UserAggregate(UUIDValueObject userId, EmailValueObject email, PasswordHashValueObject passwordHash) {
        this.userId = userId;
        this.email = email;
        this.passwordHash = passwordHash;
        this.verified = false;
        this.failedLoginAttempts = 0;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Constructor for RECONSTITUTING a UserAggregate from the database.
     * Used by Repository Adapters to rebuild the aggregate from SQL state.
     *
     * @param userId                     Existing unique identifier
     * @param email                      Existing email address
     * @param passwordHash               Existing password hash
     * @param verified                   Current verification status
     * @param failedLoginAttempts        Current count of failed attempts
     * @param createdAt                  Original creation timestamp
     * @param updatedAt                  Last modified timestamp
     * @param lastPasswordResetRequestAt Last reset request timestamp (can be null)
     */
    UserAggregate(UUIDValueObject userId,
            EmailValueObject email,
            PasswordHashValueObject passwordHash,
            boolean verified,
            int failedLoginAttempts,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            LocalDateTime lastPasswordResetRequestAt) {
        this.userId = userId;
        this.email = email;
        this.passwordHash = passwordHash;
        this.verified = verified;
        this.failedLoginAttempts = failedLoginAttempts;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.lastPasswordResetRequestAt = lastPasswordResetRequestAt;
    }

    /**
     * Verifies the user's email using a provided verification token.
     * Checks if the user is already verified and if the token is valid for this
     * specific user.
     *
     * @param token The verification token aggregate to validate
     * @return true if verification succeeded, false if the token is null or invalid
     * @throws IllegalStateException if the user has already completed verification
     */
    public boolean verifyEmail(VerificationTokenAggregate token) {
        if (this.verified) {
            throw new IllegalStateException("User is already verified.");
        }

        if (token == null || !token.isValidFor(this.userId)) {
            return false;
        }

        this.verified = true;
        this.updatedAt = LocalDateTime.now();
        return true;
    }

    /**
     * Resets the user's password using a valid reset token.
     * Upon success, resets the failed login counter to allow immediate access.
     *
     * @param token       The password reset token to validate
     * @param newPassword The new hashed password to apply to the account
     * @return true if the reset succeeded, false if the token is null or invalid
     */
    public boolean resetPassword(PasswordResetTokenAggregate token, PasswordHashValueObject newPassword) {
        if (token == null || !token.isValidFor(this.userId)) {
            return false;
        }

        this.passwordHash = newPassword;
        this.failedLoginAttempts = 0;
        this.updatedAt = LocalDateTime.now();
        return true;
    }

    /**
     * Increments the failed login attempt counter based on a verified failure
     * proof.
     * 
     * @param proof The failure certificate issued by the LoginValidationService
     * 
     * @throws IllegalArgumentException if the proof is for a different user
     */
    public void recordFailedLogin(FailedAuthProof proof) {
        // Safety check: Ensure the proof actually belongs to THIS user instance
        proof.getUser().ifPresent(userFromProof -> {
            if (!userFromProof.getUserId().equals(this.userId)) {
                throw new IllegalArgumentException("Proof mismatch: Cannot record failure using another user's proof.");
            }

            this.failedLoginAttempts++;
            this.updatedAt = LocalDateTime.now();
        });
    }

    /**
     * Resets the failed login counter to zero based on a verified success proof.
     * 
     * @param proof The success certificate issued by the LoginValidationService
     * 
     * @throws IllegalArgumentException if the proof is for a different user
     */
    public void resetFailedLogins(SuccessfulAuthProof proof) {
        // Safety check: Ensure the proof actually belongs to THIS user instance
        proof.getUser().ifPresent(userFromProof -> {
            if (!userFromProof.getUserId().equals(this.userId)) {
                throw new IllegalArgumentException(
                        "Proof mismatch: Cannot reset login attempts using another user's proof.");
            }

            this.failedLoginAttempts = 0;
            this.updatedAt = LocalDateTime.now();
        });
    }

    /**
     * Determines if the user account is currently locked based on failed attempts.
     *
     * @return true if the failed attempts counter meets or exceeds the maximum
     *         limit
     */
    public boolean isLocked() {
        return this.failedLoginAttempts >= MAX_FAILED_ATTEMPTS;
    }

    /**
     * Checks if enough time has passed since the last password reset request.
     *
     * @return true if the cooldown period has elapsed or no prior request exists
     */
    public boolean canRequestPasswordReset() {
        if (lastPasswordResetRequestAt == null) {
            return true;
        }

        return Duration.between(lastPasswordResetRequestAt, LocalDateTime.now())
                .toMinutes() >= PASSWORD_RESET_COOLDOWN_MINUTES;
    }

    /**
     * Records a new password reset request if the user is currently eligible.
     *
     * @return true if the request was successfully recorded, false if within
     *         cooldown
     */
    public boolean requestPasswordReset() {
        if (!canRequestPasswordReset()) {
            return false;
        }

        this.lastPasswordResetRequestAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        return true;
    }

    /**
     * Gets the unique identifier for this user.
     *
     * @return the userId value object
     */
    public UUIDValueObject getUserId() {
        return userId;
    }

    /**
     * Gets the user's email address.
     *
     * @return the email value object
     */
    public EmailValueObject getEmail() {
        return email;
    }

    /**
     * Gets the current password hash.
     *
     * @return the password hash value object
     */
    public PasswordHashValueObject getPasswordHash() {
        return passwordHash;
    }

    /**
     * Gets the verification status of the account.
     *
     * @return true if verified, false otherwise
     */
    public boolean isVerified() {
        return verified;
    }

    /**
     * Gets the number of recorded failed login attempts.
     *
     * @return the total count of failed attempts
     */
    public int getFailedLoginAttempts() {
        return failedLoginAttempts;
    }

    /**
     * Gets the timestamp of account creation.
     *
     * @return the creation LocalDateTime
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Gets the timestamp of the last state change.
     *
     * @return the last updated LocalDateTime
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Gets the timestamp of the most recent password reset request.
     *
     * @return the request LocalDateTime, or null if never requested
     */
    public LocalDateTime getLastPasswordResetRequestAt() {
        return lastPasswordResetRequestAt;
    }
}