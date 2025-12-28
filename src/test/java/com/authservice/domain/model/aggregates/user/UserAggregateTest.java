package com.authservice.domain.model.aggregates.user;

import com.authservice.domain.model.services.credentialService.login.*;
import com.authservice.domain.model.valueobjects.*;
import com.authservice.domain.ports.IHashingService;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@DisplayName("User Aggregate - Security & Proof Invariants")
class UserAggregateTest {

    private UserAggregate user;
    private IHashingService mockHasher;
    private final String TEST_EMAIL = "mohamed@gatech.edu";
    private final String TEST_HASH = "$2b$12$ValidHashForTesting";

    @BeforeEach
    void setUp() {
        // 1. Mock the Hashing Service
        mockHasher = Mockito.mock(IHashingService.class);

        // 2. Setup the mock behavior for reconstitution validation
        when(mockHasher.isAlreadyHashed(anyString())).thenReturn(true);

        // 3. Create the PasswordHashValueObject via Reconstitution
        PasswordHashValueObject hashVO = PasswordHashValueObject.reconstitute(TEST_HASH, mockHasher);

        // 4. Initialize the Aggregate (using package-private constructor access)
        user = new UserAggregate(
                new UUIDValueObject(UUID.randomUUID().toString()),
                new EmailValueObject(TEST_EMAIL),
                hashVO);
    }

    @Nested
    @DisplayName("Login Security via Proofs")
    class LoginSecurityTests {

        @Test
        @DisplayName("Should increment failed attempts when a valid FailedAuthProof is provided")
        void shouldRecordFailure() {
            FailedAuthProof proof = AuthProofTestBridge.createFailure(user, AuthFailureReason.INVALID_CREDENTIALS);

            user.recordFailedLogin(proof);

            assertThat(user.getFailedLoginAttempts()).isEqualTo(1);
            assertThat(user.isLocked()).isFalse();
        }

        @Test
        @DisplayName("Should lock account after 5 recorded failures")
        void shouldLockAccount() {
            FailedAuthProof proof = AuthProofTestBridge.createFailure(user, AuthFailureReason.INVALID_CREDENTIALS);

            for (int i = 0; i < 5; i++) {
                user.recordFailedLogin(proof);
            }

            assertThat(user.isLocked()).isTrue();
        }

        @Test
        @DisplayName("Should reset attempts when a valid SuccessfulAuthProof is provided")
        void shouldResetAttempts() {
            FailedAuthProof failProof = AuthProofTestBridge.createFailure(user, AuthFailureReason.INVALID_CREDENTIALS);
            user.recordFailedLogin(failProof);
            user.recordFailedLogin(failProof);

            SuccessfulAuthProof successProof = AuthProofTestBridge.createSuccess(user);
            user.resetFailedLogins(successProof);

            assertThat(user.getFailedLoginAttempts()).isZero();
        }

        @Test
        @DisplayName("Security Guard: Should throw exception if proof belongs to a different user")
        void shouldRejectMismatchedProof() {
            // Create a different user with the same mock setup
            UserAggregate stranger = new UserAggregate(
                    new UUIDValueObject(UUID.randomUUID().toString()),
                    new EmailValueObject("stranger@gatech.edu"),
                    PasswordHashValueObject.reconstitute(TEST_HASH, mockHasher));

            SuccessfulAuthProof proofForStranger = AuthProofTestBridge.createSuccess(stranger);

            assertThatThrownBy(() -> user.resetFailedLogins(proofForStranger))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Proof mismatch");
        }
    }

    @Nested
    @DisplayName("Password Reset Constraints")
    class ResetTests {
        @Test
        @DisplayName("Should enforce cooldown period between reset requests")
        void shouldEnforceResetCooldown() {
            assertThat(user.requestPasswordReset()).isTrue();
            assertThat(user.canRequestPasswordReset()).isFalse();
            assertThat(user.requestPasswordReset()).isFalse();
        }
    }
}