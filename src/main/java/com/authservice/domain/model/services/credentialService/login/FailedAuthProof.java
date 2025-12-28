package com.authservice.domain.model.services.credentialService.login;

import com.authservice.domain.model.aggregates.user.UserAggregate;

/**
 * Proof that a credential validation failed.
 */
public class FailedAuthProof extends AuthProof {

    private final AuthFailureReason reason;

    /**
     * Creates a proof of failed authentication with a specific typed reason.
     * Constructor is package-private to prevent manual creation outside the
     * service.
     * 
     * @param user   the user associated with the login request
     * @param reason the specific category of failure
     */
    FailedAuthProof(UserAggregate user, AuthFailureReason reason) {
        super(user);
        this.reason = reason;
    }

    /**
     * Returns the typed reason for the authentication failure.
     *
     * @return the failure reason enum
     */
    public AuthFailureReason getReason() {
        return reason;
    }
}