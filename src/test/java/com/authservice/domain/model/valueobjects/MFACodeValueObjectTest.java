package com.authservice.domain.model.valueobjects;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MFACodeValueObjectTest {

    // --------- Valid MFA Codes ---------
    @Test
    void constructor_shouldAcceptValidCodes() {
        int[] validCodes = { 0, 1, 123456, 999999, 42 };
        for (int code : validCodes) {
            MFACodeValueObject mfa = new MFACodeValueObject(code);
            assertEquals(code, mfa.getValue());
        }
    }

    // --------- Invalid MFA Codes ---------
    @Test
    void constructor_shouldThrowExceptionForInvalidCodes() {
        int[] invalidCodes = { -1, -100, 1000000, 1000001 };
        for (int code : invalidCodes) {
            assertThrows(IllegalArgumentException.class,
                    () -> new MFACodeValueObject(code),
                    "Code should be invalid: " + code);
        }
    }

    // --------- Zero-padded string representation ---------
    @Test
    void getCodeString_shouldReturnZeroPaddedString() {
        MFACodeValueObject mfa1 = new MFACodeValueObject(0);
        assertEquals("000000", mfa1.getCodeString());

        MFACodeValueObject mfa2 = new MFACodeValueObject(42);
        assertEquals("000042", mfa2.getCodeString());

        MFACodeValueObject mfa3 = new MFACodeValueObject(123456);
        assertEquals("123456", mfa3.getCodeString());

        MFACodeValueObject mfa4 = new MFACodeValueObject(999999);
        assertEquals("999999", mfa4.getCodeString());
    }
}
