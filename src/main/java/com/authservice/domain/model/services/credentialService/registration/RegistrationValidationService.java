package com.authservice.domain.model.services.credentialService.registration;

import com.authservice.domain.model.services.credentialService.CredentialValidationService;
import com.authservice.domain.model.valueobjects.EmailValueObject;
import com.authservice.domain.model.valueobjects.PasswordValueObject;
import com.authservice.domain.ports.IUserRepository;

/**
 * Domain service responsible for validating new user registrations.
 * <p>
 * This service enforces structural and business rules for registration,
 * including email/password validation and uniqueness checks.
 * Upon successful validation, it issues a {@link RegistrationProof},
 * which serves as the one-time certificate required to create a user.
 * </p>
 */
public class RegistrationValidationService extends CredentialValidationService {

    /**
     * Constructs a new RegistrationValidationService with the given repository.
     *
     * @param userRepository the repository used to check for existing users
     */
    public RegistrationValidationService(IUserRepository userRepository) {
        super(userRepository);
    }

    /**
     * Validates a registration attempt.
     * <p>
     * Performs structural validation of the email and password, ensures
     * that the email is unique, and issues a {@link RegistrationProof} if
     * all checks pass.
     * </p>
     *
     * @param email    the raw email string to register
     * @param password the raw password string to register
     * @return a {@link RegistrationProof} representing authorization to create
     *         a new user account
     * @throws IllegalArgumentException if the email is already in use or if
     *                                  structural rules are violated
     */
    public RegistrationProof validateForRegistration(String email, String password) {
        // 1. Structural checks (Regex/Similarity)
        validate(email, password);

        EmailValueObject emailVO = new EmailValueObject(email);
        PasswordValueObject passwordVO = new PasswordValueObject(password);

        // 2. Uniqueness check
        if (userRepository.existsByEmail(emailVO)) {
            throw new IllegalArgumentException("Email already exists.");
        }

        // 3. Issue the Certificate
        return new RegistrationProof(emailVO, passwordVO);
    }
}
