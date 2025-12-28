package com.authservice.domain.model.services.credentialService.login;

import com.authservice.domain.model.aggregates.user.UserAggregate;
import com.authservice.domain.model.aggregates.user.UserAggregateTestBridge;
import com.authservice.domain.model.valueobjects.EmailValueObject;
import com.authservice.domain.model.valueobjects.PasswordHashValueObject;
import com.authservice.domain.model.valueobjects.UUIDValueObject;
import com.authservice.domain.ports.IHashingService;
import com.authservice.domain.ports.IUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@DisplayName("Login Validation Service - Comprehensive Tests")
class LoginValidationServiceTest {

    private IUserRepository userRepository;
    private IHashingService hashingService;
    private LoginValidationService loginService;
    private UserAggregate realUser;

    private final String TEST_EMAIL = "mohamed@gatech.edu";
    private final String TEST_PASS = "ValidPass123!.";
    private final String TEST_HASH = "$2b$12$ValidHashForTesting";

    @BeforeEach
    void setUp() {
        userRepository = Mockito.mock(IUserRepository.class);
        hashingService = Mockito.mock(IHashingService.class);
        loginService = new LoginValidationService(userRepository, hashingService);

        when(hashingService.isAlreadyHashed(TEST_HASH)).thenReturn(true);

        PasswordHashValueObject hashVO = PasswordHashValueObject.reconstitute(TEST_HASH, hashingService);

        realUser = UserAggregateTestBridge.create(
                new UUIDValueObject(UUID.randomUUID().toString()),
                new EmailValueObject(TEST_EMAIL),
                hashVO);
    }

    @Test
    @DisplayName("Structure: Should throw exception for invalid email format immediately")
    void shouldFailForBadEmailFormat() {
        assertThatThrownBy(() -> loginService.validateForLogin("invalid-email", TEST_PASS))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid email format");
    }

    @Test
    @DisplayName("Structure: Should throw exception if password is too similar to email handle")
    void shouldFailForSimilarPassword() {
        assertThatThrownBy(() -> loginService.validateForLogin(TEST_EMAIL, "mohaMed123!."))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("too similar");
    }

    @Test
    @DisplayName("Success: Should issue SuccessfulAuthProof linked to the UserAggregate")
    void shouldIssueSuccessProof() {
        when(userRepository.findByEmail(any())).thenReturn(Optional.of(realUser));
        when(hashingService.verify(eq(TEST_PASS), eq(TEST_HASH))).thenReturn(true);

        AuthProof result = loginService.validateForLogin(TEST_EMAIL, TEST_PASS);

        assertThat(result).isInstanceOf(SuccessfulAuthProof.class);
        assertThat(result.getUser()).isPresent().contains(realUser);
    }

    @Test
    @DisplayName("Security: Should issue FailedAuthProof with empty User for missing user")
    void shouldIssueFailureForMissingUser() {
        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());

        AuthProof result = loginService.validateForLogin("stranger@gatech.edu", TEST_PASS);

        assertThat(result).isInstanceOf(FailedAuthProof.class);
        assertThat(result.getUser()).isEmpty();
        assertThat(((FailedAuthProof) result).getReason()).isEqualTo(AuthFailureReason.INVALID_CREDENTIALS);
    }

    @Test
    @DisplayName("Security: Should issue FailedAuthProof linked to user when account is locked")
    void shouldIssueFailureForLockedAccount() {
        // --- FIX STARTS HERE ---
        // 1. Create a proof using the bridge to "authorize" the state change
        FailedAuthProof proof = AuthProofTestBridge.createFailure(realUser, AuthFailureReason.INVALID_CREDENTIALS);

        // 2. Pass the proof to the method (satisfying the compiler)
        for (int i = 0; i < 5; i++) {
            realUser.recordFailedLogin(proof);
        }
        // --- FIX ENDS HERE ---

        when(userRepository.findByEmail(any())).thenReturn(Optional.of(realUser));

        AuthProof result = loginService.validateForLogin(TEST_EMAIL, TEST_PASS);

        assertThat(result).isInstanceOf(FailedAuthProof.class);
        assertThat(result.getUser()).isPresent().contains(realUser);
        assertThat(((FailedAuthProof) result).getReason()).isEqualTo(AuthFailureReason.ACCOUNT_LOCKED);
    }

    @Test
    @DisplayName("Security: Should issue FailedAuthProof linked to user when password is wrong")
    void shouldIssueFailureForWrongPassword() {
        when(userRepository.findByEmail(any())).thenReturn(Optional.of(realUser));
        when(hashingService.verify(any(), eq(TEST_HASH))).thenReturn(false);

        AuthProof result = loginService.validateForLogin(TEST_EMAIL, "WrongPass123!.");

        assertThat(result).isInstanceOf(FailedAuthProof.class);
        assertThat(result.getUser()).isPresent().contains(realUser);
        assertThat(((FailedAuthProof) result).getReason()).isEqualTo(AuthFailureReason.INVALID_CREDENTIALS);
    }
}