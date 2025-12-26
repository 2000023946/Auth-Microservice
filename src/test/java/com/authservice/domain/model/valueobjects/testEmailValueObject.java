package com.authservice.domain.model.valueobjects;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import com.authservice.domain.model.valueobjects.EmailValueObject;

class EmailTest {

    @Test
    void validEmail_shouldCreateEmailObject() {
        EmailValueObject email = new EmailValueObject("user@example.com");
        assertEquals("user@example.com", email.getValue());
    }

    @Test
    void validEmail_withSubdomain_shouldCreateEmailObject() {
        EmailValueObject email = new EmailValueObject("user@mail.example.co.uk");
        assertEquals("user@mail.example.co.uk", email.getValue());
    }

    @Test
    void validEmail_withUpperCase_shouldNormalizeToLowerCase() {
        EmailValueObject email = new EmailValueObject("USER@Example.COM");
        assertEquals("user@example.com", email.getValue());
    }

    @Test
    void validEmail_withLeadingAndTrailingSpaces_shouldTrim() {
        EmailValueObject email = new EmailValueObject("   user@example.com   ");
        assertEquals("user@example.com", email.getValue());
    }

    @Test
    void invalidEmail_missingAtSymbol_shouldThrowException() {
        assertThrows(IllegalArgumentException.class,
                () -> new EmailValueObject("userexample.com"));
    }

    @Test
    void invalidEmail_multipleAtSymbols_shouldThrowException() {
        assertThrows(IllegalArgumentException.class,
                () -> new EmailValueObject("user@@example.com"));
    }

    @Test
    void invalidEmail_missingLocalPart_shouldThrowException() {
        assertThrows(IllegalArgumentException.class,
                () -> new EmailValueObject("@example.com"));
    }

    @Test
    void invalidEmail_missingDomain_shouldThrowException() {
        assertThrows(IllegalArgumentException.class,
                () -> new EmailValueObject("user@"));
    }

    @Test
    void invalidEmail_emptyString_shouldThrowException() {
        assertThrows(IllegalArgumentException.class,
                () -> new EmailValueObject(""));
    }

    @Test
    void invalidEmail_null_shouldThrowException() {
        assertThrows(IllegalArgumentException.class,
                () -> new EmailValueObject(null));
    }

    @Test
    void equalityCheck_shouldBeTrueForSameEmail() {
        EmailValueObject email1 = new EmailValueObject("user@example.com");
        EmailValueObject email2 = new EmailValueObject("USER@example.com");
        System.out.println(email1.equals(email2));
        assertTrue(email1.equals(email2));

    }

    @Test
    void equalityCheck_shouldBeFalseForDifferentEmails() {
        EmailValueObject email1 = new EmailValueObject("user1@example.com");
        EmailValueObject email2 = new EmailValueObject("user2@example.com");
        assertNotEquals(email1, email2);
    }
}
