package com.authservice.domain.model.aggregates;

import com.authservice.domain.model.valueobjects.UUIDValueObject;
import com.authservice.domain.model.aggregates.mfaToken.MFATokenAggregate;
import com.authservice.domain.model.aggregates.mfaToken.MFATokenFactory;

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
 * Unified test for the MFAToken Domain Capsule.
 * residing in the same package to maintain access to the aggregate's internal
 * state
 * while verifying the Factory's orchestration.
 */
@DisplayName("MFA Token Domain Capsule Tests")
class MFATokenAggregateTest {

    private MFATokenFactory factory;
    private final String VALID_USER_ID = UUID.randomUUID().toString();
    private final String VALID_TOKEN_ID = UUID.randomUUID().toString();
    private final int RAW_MFA_CODE = 123456;
    private final String STRING_MFA_CODE = "123456";

    @BeforeEach
    void setUp() {
        factory = new MFATokenFactory();
    }

    @Nested
    @DisplayName("Factory: Creation (New MFA Challenges)")
    class FactoryCreationTests {

        @Test
        @DisplayName("Should create a brand new MFA token aggregate from raw code and user ID")
        void shouldCreateNewMFAToken() {
            // MFA is usually short-lived (e.g., 5 minutes)
            LocalDateTime expiry = LocalDateTime.now().plusMinutes(5);

            MFATokenAggregate token = factory.create(RAW_MFA_CODE, expiry, VALID_USER_ID);

            assertThat(token).isNotNull();
            assertThat(token.getTokenId()).isNotNull();
            assertThat(token.getValue().getValue()).isEqualTo(RAW_MFA_CODE);
            assertThat(token.getUserId().getValue()).isEqualTo(VALID_USER_ID);
            assertThat(token.isActive()).isTrue();
        }

        @Test
        @DisplayName("Should fail if the provided User ID string is not a valid UUID")
        void shouldFailOnInvalidUserId() {
            LocalDateTime expiry = LocalDateTime.now().plusMinutes(5);

            assertThatThrownBy(() -> factory.create(RAW_MFA_CODE, expiry, "not-a-uuid"))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("Factory: Reconstitution (Persistence)")
    class FactoryReconstitutionTests {

        @Test
        @DisplayName("Should parse raw strings from DB and rebuild the aggregate exactly")
        void shouldReconstituteFromStrings() {
            // Using relative time strings to prevent expiration issues during test runs
            String issuedAtStr = LocalDateTime.now().minusMinutes(2).toString();
            String expiresAtStr = LocalDateTime.now().plusMinutes(3).toString();

            MFATokenAggregate token = factory.reconstitute(
                    VALID_TOKEN_ID, RAW_MFA_CODE, issuedAtStr, expiresAtStr, false, VALID_USER_ID);

            assertThat(token.getTokenId().getValue()).isEqualTo(VALID_TOKEN_ID);
            assertThat(token.getIssuedAt()).isEqualTo(LocalDateTime.parse(issuedAtStr));
            assertThat(token.getValue().getValue()).isEqualTo(RAW_MFA_CODE);
            assertThat(token.isActive()).isTrue();
        }

        @Test
        @DisplayName("Should throw DateTimeParseException if persistence layer provides invalid date strings")
        void shouldFailOnMalformedDateStrings() {
            assertThatThrownBy(() -> factory.reconstitute(
                    VALID_TOKEN_ID, RAW_MFA_CODE, "2025/12/27", "invalid-time", false, VALID_USER_ID))
                    .isInstanceOf(DateTimeParseException.class);
        }
    }

    @Nested
    @DisplayName("Aggregate: Business Logic & Security")
    class AggregateLogicTests {

        @Test
        @DisplayName("isValidFor should strictly verify the user ID and active status")
        void testIsValidForLogic() {
            LocalDateTime expiry = LocalDateTime.now().plusMinutes(5);
            MFATokenAggregate token = factory.create(RAW_MFA_CODE, expiry, VALID_USER_ID);

            // 1. Correct Match
            assertThat(token.isValidFor(new UUIDValueObject(VALID_USER_ID))).isTrue();

            // 2. Wrong User attempting to verify
            UUIDValueObject intruderId = new UUIDValueObject(UUID.randomUUID().toString());
            assertThat(token.isValidFor(intruderId)).isFalse();

            // 3. Correct User but token has been revoked (e.g., too many attempts)
            token.revoke();
            assertThat(token.isValidFor(new UUIDValueObject(VALID_USER_ID))).isFalse();
        }

        @Test
        @DisplayName("Aggregate should report inactive once the short MFA window closes")
        void testTemporalExpiration() {
            // Simulating an expired state from 10 minutes ago using the Factory
            String pastIssue = LocalDateTime.now().minusMinutes(15).toString();
            String pastExpiry = LocalDateTime.now().minusMinutes(10).toString();

            MFATokenAggregate expiredToken = factory.reconstitute(
                    VALID_TOKEN_ID, RAW_MFA_CODE, pastIssue, pastExpiry, false, VALID_USER_ID);

            assertThat(expiredToken.isActive()).isFalse();
        }
    }
}