package com.authservice.domain.model.valueobjects;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UUIDValueObjectTest {

    // --------- Valid UUID ---------
    @Test
    void constructor_shouldAcceptValidUUID() {
        String uuidStr = UUID.randomUUID().toString();
        UUIDValueObject uuid = new UUIDValueObject(uuidStr);
        assertEquals(uuidStr, uuid.getValue());
    }

    // --------- Invalid UUID ---------
    @Test
    void constructor_shouldThrowExceptionForInvalidUUID() {
        String invalidUUID = "12345-invalid-uuid";
        assertThrows(IllegalArgumentException.class,
                () -> new UUIDValueObject(invalidUUID));
    }

    // --------- Null input ---------
    @Test
    void constructor_shouldThrowExceptionForNull() {
        assertThrows(IllegalArgumentException.class,
                () -> new UUIDValueObject(null));
    }

    // --------- Random UUID generation ---------
    @Test
    void generate_shouldReturnValidRandomUUID() {
        UUIDValueObject uuid1 = UUIDValueObject.generate();
        UUIDValueObject uuid2 = UUIDValueObject.generate();

        assertNotNull(uuid1);
        assertNotNull(uuid2);
        assertNotEquals(uuid1.getValue(), uuid2.getValue());

        // Validate format
        UUID.fromString(uuid1.getValue());
        UUID.fromString(uuid2.getValue());
    }
}
