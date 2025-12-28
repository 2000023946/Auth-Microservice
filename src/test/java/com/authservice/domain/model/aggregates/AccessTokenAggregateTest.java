package com.authservice.domain.model.aggregates;

import com.authservice.domain.model.valueobjects.UUIDValueObject;
import com.authservice.domain.model.aggregates.accessToken.AccessTokenAggregate;
import com.authservice.domain.model.aggregates.accessToken.AccessTokenFactory;

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
 * Unified test for the AccessToken Domain Capsule.
 * residing in the same package to test both the Factory and the Aggregate.
 */
@DisplayName("Access Token Domain Capsule Tests")
class AccessTokenAggregateTest {

    private AccessTokenFactory factory;
    private final String VALID_SESSION_ID = UUID.randomUUID().toString();
    private final String VALID_TOKEN_ID = UUID.randomUUID().toString();
    private final String JWT_VAL = "eyJhbGciOiJIUzI1NiJ9.demo.payload";

    @BeforeEach
    void setUp() {
        factory = new AccessTokenFactory();
    }

    @Nested
    @DisplayName("Factory: Creation (New Tokens)")
    class FactoryCreationTests {

        @Test
        @DisplayName("Should create a new aggregate from raw inputs and handle internal ID generation")
        void shouldCreateNewToken() {
            LocalDateTime expiry = LocalDateTime.now().plusMinutes(15);

            AccessTokenAggregate token = factory.create(JWT_VAL, expiry, VALID_SESSION_ID);

            assertThat(token).isNotNull();
            assertThat(token.getValue()).isEqualTo(JWT_VAL);
            assertThat(token.getSessionId().getValue()).isEqualTo(VALID_SESSION_ID);
            assertThat(token.getTokenId()).isNotNull(); // Check UUID generated
            assertThat(token.isActive()).isTrue();
        }

        @Test
        @DisplayName("Should fail if raw Session ID is not a valid UUID")
        void shouldFailOnInvalidSessionId() {
            LocalDateTime expiry = LocalDateTime.now().plusMinutes(15);

            assertThatThrownBy(() -> factory.create(JWT_VAL, expiry, "not-a-uuid"))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("Factory: Reconstitution (Database Persistence)")
    class FactoryReconstitutionTests {

        @Test
        @DisplayName("Should parse raw strings and rebuild the aggregate exactly as stored")
        void shouldReconstituteFromStrings() {
            // Arrange: Create times relative to "now" so the test never expires
            LocalDateTime now = LocalDateTime.now();
            String issuedAtStr = now.minusMinutes(5).toString();
            String expiresAtStr = now.plusMinutes(15).toString(); // Guaranteed to be in the future

            // Act
            AccessTokenAggregate token = factory.reconstitute(
                    VALID_TOKEN_ID,
                    JWT_VAL,
                    issuedAtStr,
                    expiresAtStr,
                    false,
                    VALID_SESSION_ID);

            // Assert
            assertThat(token.getTokenId().getValue()).isEqualTo(VALID_TOKEN_ID);

            // Use the parsed object for comparison to avoid nanosecond precision mismatches
            assertThat(token.getIssuedAt()).isEqualTo(LocalDateTime.parse(issuedAtStr));

            // This will now pass because expiresAtStr is 15 minutes in the future
            assertThat(token.isActive()).isTrue();
        }

        @Test
        @DisplayName("Should throw DateTimeParseException if DB returns malformed ISO dates")
        void shouldFailOnBadDateStrings() {
            assertThatThrownBy(() -> factory.reconstitute(
                    VALID_TOKEN_ID, JWT_VAL, "2025/12/27", "invalid-date", false, VALID_SESSION_ID))
                    .isInstanceOf(DateTimeParseException.class);
        }
    }

    @Nested
    @DisplayName("Aggregate: Business Logic & Invariants")
    class AggregateLogicTests {

        @Test
        @DisplayName("isValidFor should strictly enforce both active status and session pinning")
        void testIsValidForLogic() {
            LocalDateTime expiry = LocalDateTime.now().plusMinutes(15);
            AccessTokenAggregate token = factory.create(JWT_VAL, expiry, VALID_SESSION_ID);

            // 1. Success case
            assertThat(token.isValidFor(new UUIDValueObject(VALID_SESSION_ID))).isTrue();

            // 2. Wrong session ID
            UUIDValueObject randomSession = new UUIDValueObject(UUID.randomUUID().toString());
            assertThat(token.isValidFor(randomSession)).isFalse();

            // 3. Right session but revoked
            token.revoke();
            assertThat(token.isValidFor(new UUIDValueObject(VALID_SESSION_ID))).isFalse();
        }

        @Test
        @DisplayName("Aggregate should compute isActive based on temporal state")
        void testTemporalExpiration() {
            // Arrange: Use strings for the factory's reconstitute method
            String pastIssue = LocalDateTime.now().minusMinutes(20).toString();
            String pastExpiry = LocalDateTime.now().minusMinutes(5).toString();

            // Act: Use the factory (which is public) instead of 'new'
            AccessTokenAggregate expiredToken = factory.reconstitute(
                    VALID_TOKEN_ID,
                    JWT_VAL,
                    pastIssue,
                    pastExpiry,
                    false,
                    VALID_SESSION_ID);

            // Assert
            assertThat(expiredToken.isActive()).isFalse();
        }
    }
}