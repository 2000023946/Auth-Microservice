package com.authservice.domain.model.services;

import java.util.List;

import com.authservice.domain.model.entities.userLoginInformation.UserLoginInformation;

/**
 * Domain Service responsible for determining if multi-factor authentication
 * (MFA)
 * is required based on the security context of the login attempt.
 * <p>
 * This service implements risk-based authentication by comparing the current
 * login attempt against a history of known, successful login contexts.
 */
public class MFAService {

    /**
     * Determines if an MFA challenge should be issued for the current login
     * attempt.
     * <p>
     * The decision is based on the "Recognized Context" rule: if the current
     * combination of IP Address and User Agent (Device/Browser/OS) has not been
     * seen before in the user's history, the attempt is flagged as "at risk."
     *
     * @param history      a list of historical {@link UserLoginInformation} entries
     *                     representing previous successful logins.
     * @param currentLogin the {@link UserLoginInformation} for the current attempt.
     * @return {@code true} if MFA is required (new context); {@code false} if the
     *         context is recognized.
     * @throws IllegalArgumentException if currentLogin is null.
     */
    public boolean isMFARequired(List<UserLoginInformation> history, UserLoginInformation currentLogin) {
        if (currentLogin == null) {
            throw new IllegalArgumentException("Current login is null");
        }

        // If the user has no login history, we treat it as a high-risk first login.
        if (history == null || history.isEmpty()) {
            return true;
        }

        /*
         * * Since UserLoginInformation implements equals() based on its Value Objects
         * (UserId, IP, and UserAgent), we can check if the exact same context
         * has been seen before.
         */
        boolean isRecognizedAttempt = history.contains(currentLogin);

        // MFA is required if the attempt is NOT recognized in the history.
        return !isRecognizedAttempt;
    }
}