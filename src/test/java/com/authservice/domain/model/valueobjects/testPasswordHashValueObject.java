package com.authservice.domain.model.valueobjects;

import com.authservice.domain.ports.HashingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PasswordHashValueObjectTest {

    private HashingService hashingService;

    @BeforeEach
    void setUp() {
        hashingService = mock(HashingService.class);
    }

    @Test
    void constructor_shouldHashPasswordUsingHashingService() {
        String rawPassword = "mySecret123";
        String hashedPassword = "hashedValue";

        when(hashingService.hash(rawPassword)).thenReturn(hashedPassword);

        PasswordHashValueObject passwordHash = new PasswordHashValueObject(rawPassword, hashingService);

        assertEquals(hashedPassword, passwordHash.getValue());
        verify(hashingService, times(1)).hash(rawPassword);
    }

    @Test
    void constructor_shouldThrowExceptionForNullPassword() {
        assertThrows(IllegalArgumentException.class,
                () -> new PasswordHashValueObject(null, hashingService));
    }

    @Test
    void constructor_shouldThrowExceptionForBlankPassword() {
        assertThrows(IllegalArgumentException.class,
                () -> new PasswordHashValueObject("   ", hashingService));
    }

    @Test
    void constructor_shouldThrowExceptionForNullHashingService() {
        assertThrows(IllegalArgumentException.class,
                () -> new PasswordHashValueObject("password123", null));
    }

    @Test
    void equals_shouldReturnTrueForSameHashedValue() {
        String rawPassword = "secret";
        String hashedPassword = "hashedSecret";

        when(hashingService.hash(rawPassword)).thenReturn(hashedPassword);

        PasswordHashValueObject p1 = new PasswordHashValueObject(rawPassword, hashingService);
        PasswordHashValueObject p2 = new PasswordHashValueObject(rawPassword, hashingService);

        assertEquals(p1, p2);
    }

    @Test
    void hashCode_shouldBeSameForSameHashedValue() {
        String rawPassword = "secret";
        String hashedPassword = "hashedSecret";

        when(hashingService.hash(rawPassword)).thenReturn(hashedPassword);

        PasswordHashValueObject p1 = new PasswordHashValueObject(rawPassword, hashingService);
        PasswordHashValueObject p2 = new PasswordHashValueObject(rawPassword, hashingService);

        assertEquals(p1.hashCode(), p2.hashCode());
    }

    @Test
    void toString_shouldReturnHashedValue() {
        String rawPassword = "secret";
        String hashedPassword = "hashedSecret";

        when(hashingService.hash(rawPassword)).thenReturn(hashedPassword);

        PasswordHashValueObject passwordHash = new PasswordHashValueObject(rawPassword, hashingService);

        assertEquals(hashedPassword, passwordHash.toString());
    }
}
