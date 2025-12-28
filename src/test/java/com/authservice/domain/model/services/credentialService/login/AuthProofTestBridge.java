package com.authservice.domain.model.services.credentialService.login;

import com.authservice.domain.model.aggregates.user.UserAggregate;

/**
 * Bridge to instantiate package-private AuthProofs for testing.
 */
public class AuthProofTestBridge {

    public static SuccessfulAuthProof createSuccess(UserAggregate user) {
        return new SuccessfulAuthProof(user);
    }

    public static FailedAuthProof createFailure(UserAggregate user, AuthFailureReason reason) {
        return new FailedAuthProof(user, reason);
    }
}