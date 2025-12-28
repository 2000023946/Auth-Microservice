package com.authservice.domain.model.aggregates;

import com.authservice.domain.model.valueobjects.UUIDValueObject;
import com.authservice.domain.model.aggregates.session.SessionAggregate;
import com.authservice.domain.model.aggregates.session.SessionFactory;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unified test for the Session Domain Capsule.
 * Validates the Factory's ability to translate infrastructure strings into
 * UUIDValueObjects and the Aggregate's lifecycle management.
 */
@DisplayName("Session Domain Capsule Tests")
class SessionAggregateTest {

    private final String RAW_USER_ID = UUID.randomUUID().toString();
    private final String RAW_SESSION_ID = UUID.randomUUID().toString();
    private final UUIDValueObject USER_ID_VO = new UUIDValueObject(RAW_USER_ID);

    @Nested
    @DisplayName("Factory: Creation (New Sessions)")
    class FactoryCreationTests {

        @Test
        @DisplayName("Should create a new session and correctly map the User ID Value Object")
        void shouldCreateNewSession() {
            LocalDateTime expiry = LocalDateTime.now().plusHours(2);

            // Create via factory using raw string (simulating Web/API input)
            SessionAggregate session = SessionFactory.create(RAW_USER_ID, expiry);

            assertThat(session).isNotNull();
            // Verify ID is generated and wrapped in a Value Object
            assertThat(session.getSessionId()).isInstanceOf(UUIDValueObject.class);
            assertThat(session.getUserId()).isEqualTo(USER_ID_VO);
            assertThat(session.isActive()).isTrue();
        }

        @Test
        @DisplayName("Should fail if the provided User ID string violates UUID format rules")
        void shouldFailOnInvalidUserId() {
            LocalDateTime expiry = LocalDateTime.now().plusHours(2);

            assertThatThrownBy(() -> SessionFactory.create("invalid-uuid-format", expiry))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("Factory: Reconstitution (Persistence)")
    class FactoryReconstitutionTests {

        @Test
        @DisplayName("Should rebuild Value Objects from raw DB strings exactly")
        void shouldReconstituteFromStrings() {
            String createdStr = LocalDateTime.now().minusHours(1).toString();
            String activityStr = LocalDateTime.now().minusMinutes(10).toString();
            String expiresStr = LocalDateTime.now().plusHours(1).toString();

            SessionAggregate session = SessionFactory.reconstitute(
                    RAW_SESSION_ID, RAW_USER_ID, createdStr, activityStr, expiresStr, false);

            // Asserting equality against a Value Object, not just a string
            assertThat(session.getSessionId()).isEqualTo(new UUIDValueObject(RAW_SESSION_ID));
            assertThat(session.getUserId()).isEqualTo(USER_ID_VO);
            assertThat(session.isActive()).isTrue();
        }

        @Test
        @DisplayName("Should fail fast if DB returns malformed UUID strings")
        void shouldFailOnCorruptUUIDInDB() {
            String now = LocalDateTime.now().toString();

            assertThatThrownBy(() -> SessionFactory.reconstitute(
                    "not-a-uuid", RAW_USER_ID, now, now, now, false)).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should throw DateTimeParseException if DB provides invalid ISO dates")
        void shouldFailOnMalformedDateStrings() {
            assertThatThrownBy(() -> SessionFactory.reconstitute(
                    RAW_SESSION_ID, RAW_USER_ID, "2025/12/27", "invalid", "invalid", false))
                    .isInstanceOf(DateTimeParseException.class);
        }
    }

    @Nested
    @DisplayName("Aggregate: Lifecycle & Invariants")
    class AggregateLogicTests {

        @Test
        @DisplayName("Should successfully revoke session and prevent reactivation")
        void testDeactivationLifecycle() {
            SessionAggregate session = SessionFactory.create(RAW_USER_ID, LocalDateTime.now().plusHours(1));

            session.deactivate();

            assertThat(session.isRevoked()).isTrue();
            assertThat(session.isActive()).isFalse();

            // Guard against duplicate deactivation logic
            assertThatThrownBy(session::deactivate)
                    .isInstanceOf(IllegalCallerException.class);
        }

        @Test
        @DisplayName("updateLastActivity should advance the internal temporal state")
        void testActivityTracking() throws InterruptedException {
            SessionAggregate session = SessionFactory.create(RAW_USER_ID, LocalDateTime.now().plusHours(1));
            LocalDateTime firstActivity = session.getLastActivityAt();

            Thread.sleep(10); // Ensure clock tick
            session.updateLastActivity();

            assertThat(session.getLastActivityAt()).isAfter(firstActivity);
        }

        @Test
        @DisplayName("Should report inactive when the current time exceeds the expiration Value Object")
        void testExpirationLogic() {
            String pastCreated = LocalDateTime.now().minusHours(5).toString();
            String pastActivity = LocalDateTime.now().minusHours(4).toString();
            String pastExpiry = LocalDateTime.now().minusMinutes(1).toString();

            SessionAggregate expiredSession = SessionFactory.reconstitute(
                    RAW_SESSION_ID, RAW_USER_ID, pastCreated, pastActivity, pastExpiry, false);

            assertThat(expiredSession.isActive()).isFalse();
        }
    }
}