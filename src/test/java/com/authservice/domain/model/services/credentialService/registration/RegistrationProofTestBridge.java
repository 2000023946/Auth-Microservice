package com.authservice.domain.model.services.credentialService.registration;

import com.authservice.domain.model.valueobjects.EmailValueObject;
import com.authservice.domain.model.valueobjects.PasswordValueObject;

/**
 * BRIDGE: Lives in the same package as RegistrationProof to access its
 * package-private constructor for unit testing purposes.
 */
public class RegistrationProofTestBridge {
    /**
     * Mints a RegistrationProof for testing.
     * 
     * @param email    email
     * @param password password
     * 
     * @return a real RegistrationProof instance.
     */
    public static RegistrationProof create(EmailValueObject email, PasswordValueObject password) {
        return new RegistrationProof(email, password);
    }
}