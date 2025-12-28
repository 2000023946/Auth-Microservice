package com.authservice.domain.model.services.credentialService.registration;

import com.authservice.domain.model.valueobjects.EmailValueObject;
import com.authservice.domain.model.valueobjects.PasswordValueObject;

/**
 * One-time domain proof issued after successful registration validation.
 * <p>
 * This proof represents authorization to create a new user account.
 * Possession of this object is required to proceed with user creation
 * and it cannot be constructed outside the credential service.
 * </p>
 */
public final class RegistrationProof {

    /**
     * Validated email address for the new user.
     */
    private final EmailValueObject email;

    /**
     * Validated password for the new user.
     */
    private final PasswordValueObject password;

    /**
     * Creates a new registration proof.
     * <p>
     * Constructor is package-private to ensure that only
     * {@code RegistrationValidationService} can issue this proof.
     * </p>
     *
     * @param email    validated email value object
     * @param password validated password value object
     */
    RegistrationProof(EmailValueObject email, PasswordValueObject password) {
        this.email = email;
        this.password = password;
    }

    /**
     * Returns the validated email for the registration.
     *
     * @return email value object
     */
    public EmailValueObject getEmail() {
        return email;
    }

    /**
     * Returns the validated password for the registration.
     *
     * @return password value object
     */
    public PasswordValueObject getPassword() {
        return password;
    }
}
