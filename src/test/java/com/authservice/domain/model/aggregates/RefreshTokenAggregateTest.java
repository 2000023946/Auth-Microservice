package com.authservice.domain.model.aggregates;

import com.authservice.domain.model.valueobjects.UUIDValueObject;
import com.authservice.domain.model.aggregates.refreshToken.RefreshTokenAggregate;
import com.authservice.domain.model.aggregates.refreshToken.RefreshTokenFactory;

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
 * Unified test for the RefreshToken Domain Capsule.
 * residing in the same package to maintain access to package-private
 * constructors
 * while verifying the Factory's orchestration of raw infrastructure strings.
 */
@DisplayName("Refresh Token Domain Capsule Tests")
class RefreshTokenAggregateTest {

    private RefreshTokenFactory factory;
    private final String VALID_TOKEN_ID = UUID.randomUUID().toString();
    private final String VALID_SESSION_ID = UUID.randomUUID().toString();
    private final String REFRESH_VALUE = "definitely-a-secure-refresh-token-123";

    @BeforeEach
    void setUp() {
        factory = new RefreshTokenFactory();
    }

    @Nested
    @DisplayName("Factory: Creation (New Tokens)")
    class FactoryCreationTests {

        @Test
        @DisplayName("Should create a brand new refresh token from raw inputs with generated ID")
        void shouldCreateNewRefreshToken() {
            // Standard policy: 7 days
            LocalDateTime expiry = LocalDateTime.now().plusDays(7);

            RefreshTokenAggregate token = factory.create(REFRESH_VALUE, expiry, VALID_SESSION_ID);

            assertThat(token).isNotNull();
            assertThat(token.getTokenId()).isNotNull();
            assertThat(token.getValue()).isEqualTo(REFRESH_VALUE);
            assertThat(token.getSessionId().getValue()).isEqualTo(VALID_SESSION_ID);
            assertThat(token.isActive()).isTrue();
        }

        @Test
        @DisplayName("Should fail if the provided Session ID string is not a valid UUID")
        void shouldFailOnInvalidSessionId() {
            LocalDateTime expiry = LocalDateTime.now().plusDays(7);

            assertThatThrownBy(() -> factory.create(REFRESH_VALUE, expiry, "not-a-uuid"))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("Factory: Reconstitution (Database Persistence)")
    class FactoryReconstitutionTests {

        @Test
        @DisplayName("Should parse raw strings and rebuild the aggregate exactly from historical data")
        void shouldReconstituteFromStrings() {
            // Dynamic dates ensure the test passes regardless of current time
            String issuedAtStr = LocalDateTime.now().minusDays(2).toString();
            String expiresAtStr = LocalDateTime.now().plusDays(5).toString();

            RefreshTokenAggregate token = factory.reconstitute(
                    VALID_TOKEN_ID, REFRESH_VALUE, issuedAtStr, expiresAtStr, false, VALID_SESSION_ID);

            assertThat(token.getTokenId().getValue()).isEqualTo(VALID_TOKEN_ID);
            assertThat(token.getIssuedAt()).isEqualTo(LocalDateTime.parse(issuedAtStr));
            assertThat(token.getSessionId().getValue()).isEqualTo(VALID_SESSION_ID);
            assertThat(token.isActive()).isTrue();
        }

        @Test
        @DisplayName("Should throw DateTimeParseException if persistence provides invalid date strings")
        void shouldFailOnBadDateStrings() {
            assertThatThrownBy(() -> factory.reconstitute(
                    VALID_TOKEN_ID, REFRESH_VALUE, "2025-12-25", "invalid-date", false, VALID_SESSION_ID))
                    .isInstanceOf(DateTimeParseException.class);
        }
    }

    @Nested
    @DisplayName("Aggregate: Business Logic & Safety")
    class AggregateLogicTests {

        @Test
        @DisplayName("isValidFor should strictly verify the session ID and active status")
        void testIsValidForLogic() {
            LocalDateTime expiry = LocalDateTime.now().plusDays(7);
            RefreshTokenAggregate token = factory.create(REFRESH_VALUE, expiry, VALID_SESSION_ID);

            // 1. Valid case
            assertThat(token.isValidFor(new UUIDValueObject(VALID_SESSION_ID))).isTrue();

            // 2. Wrong session attempting to refresh
            UUIDValueObject intruderSession = new UUIDValueObject(UUID.randomUUID().toString());
            assertThat(token.isValidFor(intruderSession)).isFalse();

            // 3. Right session but token was revoked (e.g. session logout)
            token.revoke();
            assertThat(token.isValidFor(new UUIDValueObject(VALID_SESSION_ID))).isFalse();
        }

        @Test
        @DisplayName("Aggregate should report inactive once the refresh window has closed")
        void testTemporalExpiration() {
            // Reconstitute a token from the past that has expired
            String pastIssue = LocalDateTime.now().minusDays(10).toString();
            String pastExpiry = LocalDateTime.now().minusDays(1).toString();

            RefreshTokenAggregate expiredToken = factory.reconstitute(
                    VALID_TOKEN_ID, REFRESH_VALUE, pastIssue, pastExpiry, false, VALID_SESSION_ID);

            assertThat(expiredToken.isActive()).isFalse();
        }
    }
}