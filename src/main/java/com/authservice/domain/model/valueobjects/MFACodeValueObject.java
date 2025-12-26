package com.authservice.domain.model.valueobjects;

/**
 * MFACodeValueObject represents a 6-digit multi-factor authentication code.
 * <p>
 * This value object encapsulates validation and ensures immutability.
 * Two MFACodeValueObject instances are equal if their underlying code values
 * are equal.
 */
public class MFACodeValueObject extends ValueObject<Integer> {

    /**
     * The underlying 6-digit MFA code.
     */
    private final int code;

    /**
     * Constructs a new MFACodeValueObject after validating the code.
     *
     * @param code the 6-digit MFA code
     * @throws IllegalArgumentException if the code is not in the range 0-999999
     */
    public MFACodeValueObject(int code) {
        if (code < 0 || code > 999_999) {
            throw new IllegalArgumentException("MFA code must be a 6-digit integer between 000000 and 999999");
        }
        this.code = code;
    }

    /**
     * Returns the MFA code as an integer.
     *
     * @return the 6-digit MFA code
     */
    @Override
    public Integer getValue() {
        return code;
    }

    /**
     * Returns the MFA code as a zero-padded 6-digit string.
     *
     * @return the MFA code string (e.g., "003421")
     */
    public String getCodeString() {
        return String.format("%06d", code);
    }
}
