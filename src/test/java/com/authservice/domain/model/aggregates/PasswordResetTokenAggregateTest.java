package com.authservice.domain.model.aggregates;

import com.authservice.domain.model.valueobjects.UUIDValueObject;
import com.authservice.domain.model.aggregates.passswordResetToken.PasswordResetTokenAggregate;
import com.authservice.domain.model.aggregates.passswordResetToken.PasswordResetTokenFactory;

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
 * Unified test for the PasswordResetToken Domain Capsule.
 * residing in the same package to verify both the Factory's string parsing
 * and the Aggregate's internal business rules.
 */
@DisplayName("Password Reset Token Domain Capsule Tests")
class PasswordResetTokenAggregateTest {

    private PasswordResetTokenFactory factory;
    private final String VALID_USER_ID = UUID.randomUUID().toString();
    private final String VALID_TOKEN_ID = UUID.randomUUID().toString();
    private final String RESET_VALUE = "secure-reset-hash-789";

    @BeforeEach
    void setUp() {
        factory = new PasswordResetTokenFactory();
    }

    @Nested
    @DisplayName("Factory: Creation (New Reset Requests)")
    class FactoryCreationTests {

        @Test
        @DisplayName("Should create a new reset token with generated ID and pinned user")
        void shouldCreateNewResetToken() {
            // Policy: Password reset links expire in 1 hour
            LocalDateTime expiry = LocalDateTime.now().plusHours(1);

            PasswordResetTokenAggregate token = factory.create(RESET_VALUE, expiry, VALID_USER_ID);

            assertThat(token).isNotNull();
            assertThat(token.getTokenId()).isNotNull();
            assertThat(token.getValue()).isEqualTo(RESET_VALUE);
            assertThat(token.getUserId().getValue()).isEqualTo(VALID_USER_ID);
            assertThat(token.isActive()).isTrue();
        }

        @Test
        @DisplayName("Should fail if the provided User ID string is not a valid UUID")
        void shouldFailOnInvalidUserId() {
            LocalDateTime expiry = LocalDateTime.now().plusHours(1);

            assertThatThrownBy(() -> factory.create(RESET_VALUE, expiry, "malformed-uuid"))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("Factory: Reconstitution (Persistence)")
    class FactoryReconstitutionTests {

        @Test
        @DisplayName("Should parse raw strings and rebuild the aggregate exactly from historical data")
        void shouldReconstituteFromStrings() {
            // Using relative time strings to prevent test expiration
            String issuedAtStr = LocalDateTime.now().minusMinutes(30).toString();
            String expiresAtStr = LocalDateTime.now().plusMinutes(30).toString();

            PasswordResetTokenAggregate token = factory.reconstitute(
                    VALID_TOKEN_ID, RESET_VALUE, issuedAtStr, expiresAtStr, false, VALID_USER_ID);

            assertThat(token.getTokenId().getValue()).isEqualTo(VALID_TOKEN_ID);
            assertThat(token.getIssuedAt()).isEqualTo(LocalDateTime.parse(issuedAtStr));
            assertThat(token.getUserId().getValue()).isEqualTo(VALID_USER_ID);
            assertThat(token.isActive()).isTrue();
        }

        @Test
        @DisplayName("Should throw DateTimeParseException if DB provides invalid date strings")
        void shouldFailOnBadDateStrings() {
            assertThatThrownBy(() -> factory.reconstitute(
                    VALID_TOKEN_ID, RESET_VALUE, "not-a-date", "2025-02-30T12:00:00", false, VALID_USER_ID))
                    .isInstanceOf(DateTimeParseException.class);
        }
    }

    @Nested
    @DisplayName("Aggregate: Business Logic & Safety")
    class AggregateLogicTests {

        @Test
        @DisplayName("isValidFor should strictly verify the user ID and active status")
        void testIsValidForLogic() {
            LocalDateTime expiry = LocalDateTime.now().plusHours(1);
            PasswordResetTokenAggregate token = factory.create(RESET_VALUE, expiry, VALID_USER_ID);

            // 1. Valid case
            assertThat(token.isValidFor(new UUIDValueObject(VALID_USER_ID))).isTrue();

            // 2. Wrong user attempting to reset password
            UUIDValueObject intruderId = new UUIDValueObject(UUID.randomUUID().toString());
            assertThat(token.isValidFor(intruderId)).isFalse();

            // 3. Valid user but token was revoked (e.g., already used once)
            token.revoke();
            assertThat(token.isValidFor(new UUIDValueObject(VALID_USER_ID))).isFalse();
        }

        @Test
        @DisplayName("Aggregate should report inactive once the recovery window expires")
        void testTemporalExpiration() {
            // Reconstitute a token from the past that has expired
            String pastIssue = LocalDateTime.now().minusHours(2).toString();
            String pastExpiry = LocalDateTime.now().minusHours(1).toString();

            PasswordResetTokenAggregate expiredToken = factory.reconstitute(
                    VALID_TOKEN_ID, RESET_VALUE, pastIssue, pastExpiry, false, VALID_USER_ID);

            assertThat(expiredToken.isActive()).isFalse();
        }
    }
}