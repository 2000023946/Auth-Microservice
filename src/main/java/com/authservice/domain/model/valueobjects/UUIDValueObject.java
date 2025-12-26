package com.authservice.domain.model.valueobjects;

import java.util.UUID;

/**
 * UUIDValueObject represents an immutable UUIDv4 value within the domain.
 * <p>
 * This can be used for User IDs, Session IDs, Token IDs, or any other domain
 * entity
 * requiring a unique identifier.
 */
public class UUIDValueObject extends ValueObject<String> {

    private final String value;

    /**
     * Constructs a new UUIDValueObject after validating the UUID string.
     *
     * @param uuid the UUID string to assign
     * @throws IllegalArgumentException if the UUID is null or invalid
     */
    public UUIDValueObject(String uuid) {
        if (uuid == null) {
            throw new IllegalArgumentException("UUID cannot be null!");
        }
        try {
            UUID.fromString(uuid); // validate format
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid UUID format: " + uuid);
        }
        this.value = uuid;
    }

    /**
     * Generates a new random UUIDv4 and returns a new UUIDValueObject.
     *
     * @return a new UUIDValueObject with a random UUIDv4
     */
    public static UUIDValueObject generate() {
        return new UUIDValueObject(UUID.randomUUID().toString());
    }

    @Override
    public String getValue() {
        return value;
    }
}
