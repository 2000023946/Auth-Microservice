package com.authservice.domain.model.services.credentialService.login;

import com.authservice.domain.model.aggregates.user.UserAggregate;

/**
 * Proof that a credential validation succeeded.
 */
public class SuccessfulAuthProof extends AuthProof {

    /**
     * Creates a proof of successful authentication.
     * Constructor is package-private to prevent manual creation.
     * 
     * @param user the user associated with the login request
     */
    SuccessfulAuthProof(UserAggregate user) {
        super(user);
    }
}
