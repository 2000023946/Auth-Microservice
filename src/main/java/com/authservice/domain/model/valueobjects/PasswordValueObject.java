package com.authservice.domain.model.valueobjects;

import java.util.regex.Pattern;

/**
 * Value Object representing a raw, unhashed password.
 * <p>
 * This class enforces strict domain invariants regarding password complexity.
 * It ensures that a password cannot be instantiated unless it meets specific
 * security criteria, preventing weak credentials from entering the system.
 */
public class PasswordValueObject extends ValueObject<String> {

    /**
     * Regex Requirements:
     * - At least 8 characters
     * - At least one uppercase letter
     * - At least one lowercase letter
     * - At least one digit
     * - At least one special character (@$!%*?&.)
     */
    private static final String COMPLEXITY_REGEX = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&.])[A-Za-z\\d@$!%*?&.]{8,}$";

    private static final Pattern PASSWORD_PATTERN = Pattern.compile(COMPLEXITY_REGEX);

    private final String value;

    /**
     * Constructs a PasswordValueObject and validates complexity.
     *
     * @param value The raw password string.
     * @throws IllegalArgumentException if the password does not meet complexity
     *                                  requirements.
     */
    public PasswordValueObject(String value) {
        validate(value);
        this.value = value;
    }

    /**
     * Validates the password against domain complexity rules.
     * * @param value the string to validate
     * 
     * @throws IllegalArgumentException with a descriptive message if validation
     *                                  fails
     */
    private void validate(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Password cannot be empty.");
        }

        if (!PASSWORD_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException(
                    "Password must be at least 8 characters long, include an uppercase letter, " +
                            "a lowercase letter, a number, and a special character (@$!%*?&.).");
        }
    }

    /**
     * Returns the raw password value.
     *
     * @return the raw password string.
     */
    @Override
    public String getValue() {
        return value;
    }
}