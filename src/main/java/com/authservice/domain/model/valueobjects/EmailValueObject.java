package com.authservice.domain.model.valueobjects;

import java.util.regex.Pattern;

/**
 * EmailValueObject represents an immutable email address within the domain.
 * <p>
 * This value object encapsulates all validation, normalization, and comparison
 * rules related to an email identity. Two EmailValueObject instances are
 * considered equal if their underlying email values are equal.
 * <p>
 * This class is intended to be immutable and compared by value, not by
 * identity.
 */
public class EmailValueObject extends ValueObject<String> {
    /**
     * The underlying email value, normalized (trimmed and lowercase).
     */
    private final String value;

    /**
     * Regex pattern for basic email validation.
     */
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$",
            Pattern.CASE_INSENSITIVE);

    /**
     * Constructs a new EmailValueObject after validating and normalizing the email.
     *
     * @param email the email that is being assigned
     * @throws IllegalArgumentException if the email is null or invalid
     */
    public EmailValueObject(String email) {
        if (email == null) {
            throw new IllegalArgumentException("Argument email cannot be null!");
        }
        String normalized = email.trim().toLowerCase();
        if (!EMAIL_PATTERN.matcher(normalized).matches()) {
            throw new IllegalArgumentException("Invalid email format: " + email);
        }
        this.value = normalized;
    }

    /**
     * Returns the normalized email value.
     *
     * @return the email string
     */
    public String getValue() {
        return value;
    }

}
