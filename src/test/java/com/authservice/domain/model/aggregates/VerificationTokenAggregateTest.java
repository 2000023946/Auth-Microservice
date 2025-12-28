package com.authservice.domain.model.aggregates;

import com.authservice.domain.model.valueobjects.UUIDValueObject;
import com.authservice.domain.model.aggregates.verificationToken.VerificationTokenAggregate;
import com.authservice.domain.model.aggregates.verificationToken.VerificationTokenFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unified test for the VerificationToken Domain Capsule.
 * residing in the same package to maintain access to internal constructors
 * while verifying the Factory's orchestration of raw strings and aggregate
 * invariants.
 */
@DisplayName("Verification Token Domain Capsule Tests")
class VerificationTokenAggregateTest {

    private VerificationTokenFactory factory;
    private final String VALID_TOKEN_ID = UUID.randomUUID().toString();
    private final String VALID_USER_ID = UUID.randomUUID().toString();
    private final String VERIFY_VALUE = "secure-random-verify-token-456";

    @BeforeEach
    void setUp() {
        factory = new VerificationTokenFactory();
    }

    @Nested
    @DisplayName("Factory: Creation (New Verification Requests)")
    class FactoryCreationTests {

        @Test
        @DisplayName("Should create a brand new verification token with generated ID and pinned user")
        void shouldCreateNewVerificationToken() {
            // Policy: Verification links usually expire in 24 hours
            LocalDateTime expiry = LocalDateTime.now().plusDays(1);

            VerificationTokenAggregate token = factory.create(VERIFY_VALUE, expiry, VALID_USER_ID);

            assertThat(token).isNotNull();
            assertThat(token.getTokenId()).isNotNull();
            assertThat(token.getValue()).isEqualTo(VERIFY_VALUE);
            assertThat(token.getUserId().getValue()).isEqualTo(VALID_USER_ID);
            assertThat(token.isActive()).isTrue();
        }

        @Test
        @DisplayName("Should fail if the provided User ID string is not a valid UUID format")
        void shouldFailOnInvalidUserId() {
            LocalDateTime expiry = LocalDateTime.now().plusDays(1);

            assertThatThrownBy(() -> factory.create(VERIFY_VALUE, expiry, "not-a-uuid"))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("Factory: Reconstitution (Database Persistence)")
    class FactoryReconstitutionTests {

        @Test
        @DisplayName("Should parse raw strings and rebuild the aggregate exactly as stored in DB")
        void shouldReconstituteFromStrings() {
            // Using relative time strings to prevent test expiration errors
            String issuedAtStr = LocalDateTime.now().minusHours(2).toString();
            String expiresAtStr = LocalDateTime.now().plusHours(22).toString();

            VerificationTokenAggregate token = factory.reconstitute(
                    VALID_TOKEN_ID, VERIFY_VALUE, issuedAtStr, expiresAtStr, false, VALID_USER_ID);

            assertThat(token.getTokenId().getValue()).isEqualTo(VALID_TOKEN_ID);
            assertThat(token.getUserId().getValue()).isEqualTo(VALID_USER_ID);
            assertThat(token.getIssuedAt()).isEqualTo(LocalDateTime.parse(issuedAtStr));
            assertThat(token.isActive()).isTrue();
        }

        @Test
        @DisplayName("Should throw DateTimeParseException if persistence layer provides malformed dates")
        void shouldFailOnBadDateStrings() {
            assertThatThrownBy(() -> factory.reconstitute(
                    VALID_TOKEN_ID, VERIFY_VALUE, "2025-12-25", "invalid-timestamp", false, VALID_USER_ID))
                    .isInstanceOf(DateTimeParseException.class);
        }
    }

    @Nested
    @DisplayName("Aggregate: Business Logic & Safety")
    class AggregateLogicTests {

        @Test
        @DisplayName("isValidFor should strictly verify the owner UserID and active status")
        void testIsValidForLogic() {
            LocalDateTime expiry = LocalDateTime.now().plusHours(24);
            VerificationTokenAggregate token = factory.create(VERIFY_VALUE, expiry, VALID_USER_ID);

            // 1. Success: Correct user and active
            assertThat(token.isValidFor(new UUIDValueObject(VALID_USER_ID))).isTrue();

            // 2. Failure: Wrong user attempting to verify
            UUIDValueObject intruderId = new UUIDValueObject(UUID.randomUUID().toString());
            assertThat(token.isValidFor(intruderId)).isFalse();

            // 3. Failure: Correct user but token has been revoked
            token.revoke();
            assertThat(token.isValidFor(new UUIDValueObject(VALID_USER_ID))).isFalse();
        }

        @Test
        @DisplayName("Aggregate should compute isActive correctly based on temporal state")
        void testTemporalExpiration() {
            // Simulate an expired state from 2 days ago using the Factory
            String pastIssue = LocalDateTime.now().minusDays(3).toString();
            String pastExpiry = LocalDateTime.now().minusDays(2).toString();

            VerificationTokenAggregate expiredToken = factory.reconstitute(
                    VALID_TOKEN_ID, VERIFY_VALUE, pastIssue, pastExpiry, false, VALID_USER_ID);

            assertThat(expiredToken.isActive()).isFalse();
        }
    }
}