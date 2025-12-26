package com.authservice.domain.model.valueobjects;

import com.authservice.domain.ports.HashingService;

/**
 * Value Object representing a hashed password.
 * <p>
 * Encapsulates a hashed password value and ensures immutability.
 * Relies on a HashingService port to hash and verify passwords.
 */
public class PasswordHashValueObject extends ValueObject<String> {

    /**
     * The hashed password value.
     */
    private final String hashedValue;

    /**
     * Constructs a PasswordHashValueObject by hashing the provided raw password.
     *
     * @param rawPassword    the raw password to hash
     * @param hashingService the hashing service (port) used for hashing
     * @throws IllegalArgumentException if the raw password is null or blank,
     *                                  or if the hashing service is null
     */
    public PasswordHashValueObject(String rawPassword, HashingService hashingService) {
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException("Password cannot be null or blank");
        }
        if (hashingService == null) {
            throw new IllegalArgumentException("HashingService cannot be null");
        }
        this.hashedValue = hashingService.hash(rawPassword);
    }

    /**
     * Returns the hashed password value.
     *
     * @return the hashed password string
     */
    @Override
    public String getValue() {
        return hashedValue;
    }

}
