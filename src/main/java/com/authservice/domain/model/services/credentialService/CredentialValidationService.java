package com.authservice.domain.model.services.credentialService;

import com.authservice.domain.model.valueobjects.EmailValueObject;
import com.authservice.domain.model.valueobjects.PasswordValueObject;
import com.authservice.domain.ports.IUserRepository;

/**
 * Domain Service responsible for validating user credentials.
 * <p>
 * This abstract class serves as the base for all credential-related validation
 * logic, implementing the Template Method pattern to ensure structural
 * invariants
 * are checked before persistence-level logic is executed.
 * </p>
 */
public abstract class CredentialValidationService {

    /**
     * * The repository port used for persistence-level invariant checks
     * (e.g., uniqueness or existence).
     */
    protected final IUserRepository userRepository;

    /**
     * Constructs the base validation service with required dependencies.
     *
     * @param userRepository the repository port used to interact with user data
     */
    protected CredentialValidationService(IUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Validates the structural integrity of the provided email and password.
     * <p>
     * This process enforces individual format rules by instantiating Value Objects
     * and performs cross-field security checks. It acts as the first gatekeeper
     * in the validation pipeline.
     * </p>
     *
     * @param email    the raw email string to validate
     * @param password the raw password string to validate
     * @return true if the credentials pass all structural and cross-field checks
     * @throws IllegalArgumentException if formats are invalid or credentials are
     *                                  too similar
     */
    protected boolean validate(String email, String password) {
        // Step 1: Enforce individual domain invariants
        EmailValueObject emailVO = new EmailValueObject(email);
        PasswordValueObject passwordVO = new PasswordValueObject(password);

        // Step 2: Enforce cross-field business rules
        if (isTooSimilar(emailVO, passwordVO)) {
            throw new IllegalArgumentException("Password is too similar to the email address.");
        }

        return true;
    }

    /**
     * Internal check to prevent passwords from being derived from the email.
     * <p>
     * Protects against common vulnerabilities where users use portions of their
     * email handle as their password secret.
     * </p>
     *
     * @param email    the validated EmailValueObject
     * @param password the validated PasswordValueObject
     * @return true if the credentials are considered too similar
     */
    private boolean isTooSimilar(EmailValueObject email, PasswordValueObject password) {
        String rawEmail = email.getValue().toLowerCase();
        String rawPassword = password.getValue().toLowerCase();

        // Check if the password contains the first part of the email (before the @)
        String emailHandle = rawEmail.split("@")[0];

        if (emailHandle.length() >= 4 && rawPassword.contains(emailHandle)) {
            return true;
        }

        // Check if the password is a substring of the email or vice versa
        return rawEmail.contains(rawPassword) || rawPassword.contains(rawEmail);
    }
}