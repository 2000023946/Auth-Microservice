package com.authservice.domain.model.valueobjects;

import com.authservice.domain.ports.IHashingService;

/**
 * Value Object representing a hashed password.
 * <p>
 * This class uses static factory methods to strictly separate the logic for
 * generating new hashes from the logic of loading existing hashes from
 * persistence. It ensures that every instance contains a value that conforms
 * to the expected hashing algorithm.
 */
public class PasswordHashValueObject extends ValueObject<String> {

    private final String hashedValue;

    /**
     * Private constructor to enforce the use of factory methods.
     * 
     * @param hashedValue the hahsed string value
     */
    private PasswordHashValueObject(String hashedValue) {
        if (hashedValue == null || hashedValue.isBlank()) {
            throw new IllegalArgumentException("Hashed password value cannot be null or blank.");
        }
        this.hashedValue = hashedValue;
    }

    /**
     * FACTORY: Creates a new hash from a validated raw password.
     * Use this in Application Services during Registration or Password Change.
     *
     * @param password       The validated raw password Value Object.
     * @param hashingService The port used to perform the one-way hash.
     * @return A new PasswordHashValueObject.
     */
    public static PasswordHashValueObject create(PasswordValueObject password, IHashingService hashingService) {
        if (password == null) {
            throw new IllegalArgumentException("PasswordValueObject cannot be null.");
        }
        if (hashingService == null) {
            throw new IllegalArgumentException("HashingService cannot be null.");
        }

        String hash = hashingService.hash(password.getValue());

        // Fail-fast: ensure the hashing service actually returned a value
        if (hash == null || hash.isBlank()) {
            throw new IllegalStateException("Hashing service failed to generate a valid hash.");
        }

        return new PasswordHashValueObject(hash);
    }

    /**
     * RECONSTITUTION: Creates a Value Object from an existing hash in the database.
     * <p>
     * This method verifies that the string loaded from the database conforms to
     * the hashing algorithm's format to prevent plain-text values from entering the
     * domain.
     *
     * @param hashedValue    The raw string loaded from the database.
     * @param hashingService The port used to verify the hash format.
     * @return A reconstituted PasswordHashValueObject.
     * @throws IllegalStateException if the value from the database is not correctly
     *                               hashed.
     */
    public static PasswordHashValueObject reconstitute(String hashedValue, IHashingService hashingService) {
        if (hashingService == null) {
            throw new IllegalArgumentException("HashingService cannot be null.");
        }

        // Security Guard: Ensure the database value isn't plain-text or corrupted
        if (!hashingService.isAlreadyHashed(hashedValue)) {
            throw new IllegalStateException(
                    "Critical Error: Stored password value does not match the expected hash format. "
                            + "Possible data corruption or plain-text leak detected.");
        }

        return new PasswordHashValueObject(hashedValue);
    }

    @Override
    public String getValue() {
        return hashedValue;
    }
}