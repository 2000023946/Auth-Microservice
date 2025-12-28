package com.authservice.domain.model.entities;

import com.authservice.domain.model.valueobjects.IPAddressValueObject;
import com.authservice.domain.model.valueobjects.UUIDValueObject;
import com.authservice.domain.model.valueobjects.UserAgentValueObject;
import com.authservice.domain.model.entities.userLoginInformation.UserLoginInformation;
import com.authservice.domain.model.entities.userLoginInformation.UserLoginInformationFactory;

import com.authservice.domain.ports.IUserAgentParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * Unified test for UserLoginInformation.
 * Verifies the Factory's orchestration and the Entity's state management
 * using real Value Objects and a mocked Parser port.
 */
@DisplayName("User Login Information Domain Capsule Tests")
class UserLoginInformationTest {

    private UserLoginInformationFactory factory;
    private IUserAgentParser mockParser;

    private final String RAW_USER_ID = UUID.randomUUID().toString();
    private final String RAW_IP = "192.168.1.1";
    private final String RAW_UA = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";

    @BeforeEach
    void setUp() {
        mockParser = Mockito.mock(IUserAgentParser.class);
        factory = new UserLoginInformationFactory(mockParser);
    }

    @Nested
    @DisplayName("Factory: Creation (New Logins)")
    class CreationTests {

        @Test
        @DisplayName("Should create entity by coordinating the User-Agent parser and VOs")
        void shouldCreateViaParser() {
            // Arrange: Simulate the parser's result
            UserAgentValueObject parsedVO = UserAgentValueObject.reconstitute(
                    RAW_UA, "Mac OS X", "Chrome", "Desktop");
            when(mockParser.parse(RAW_UA)).thenReturn(parsedVO);

            // Act
            UserLoginInformation info = factory.create(RAW_USER_ID, RAW_UA, RAW_IP);

            // Assert
            assertThat(info).isNotNull();
            assertThat(info.getUserId()).isEqualTo(new UUIDValueObject(RAW_USER_ID));
            assertThat(info.getIpAddress()).isEqualTo(new IPAddressValueObject(RAW_IP));

            // Verify helper delegation
            assertThat(info.getOs()).isEqualTo("Mac OS X");
            assertThat(info.getBrowser()).isEqualTo("Chrome");
            assertThat(info.getDevice()).isEqualTo("Desktop");
        }

        @Test
        @DisplayName("Should fail creation if raw User ID is malformed")
        void shouldFailOnInvalidUserId() {
            assertThatThrownBy(() -> factory.create("not-a-uuid", RAW_UA, RAW_IP))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("Factory: Reconstitution (Database Persistence)")
    class ReconstitutionTests {

        @Test
        @DisplayName("Should rebuild entity directly from structured DB strings without parsing")
        void shouldReconstituteDirectly() {
            // Act: Using strings as they would come from a SQL result set
            UserLoginInformation info = factory.reconstitute(
                    RAW_USER_ID, RAW_UA, "iOS", "Safari", "Mobile", RAW_IP);

            // Assert
            assertThat(info.getOs()).isEqualTo("iOS");
            assertThat(info.getBrowser()).isEqualTo("Safari");
            assertThat(info.getDevice()).isEqualTo("Mobile");
            assertThat(info.getIpAddress().getValue()).isEqualTo(RAW_IP);
        }

        @Test
        @DisplayName("Should fail reconstitution if DB IP address is corrupt")
        void shouldFailOnCorruptIp() {
            assertThatThrownBy(() -> factory.reconstitute(
                    RAW_USER_ID, RAW_UA, "Windows", "Edge", "Desktop", "999.999.999.999"))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("Entity: Integrity & Equality")
    class IntegrityTests {

        @Test
        @DisplayName("Entities with identical Value Objects should be equal")
        void testEquality() {
            UserLoginInformation info1 = factory.reconstitute(
                    RAW_USER_ID, RAW_UA, "Linux", "Firefox", "Desktop", RAW_IP);
            UserLoginInformation info2 = factory.reconstitute(
                    RAW_USER_ID, RAW_UA, "Linux", "Firefox", "Desktop", RAW_IP);

            assertThat(info1).isEqualTo(info2);
            assertThat(info1.hashCode()).isEqualTo(info2.hashCode());
        }

        @Test
        @DisplayName("Entities should be unequal if the IP address Value Object differs")
        void testInequality() {
            UserLoginInformation info1 = factory.reconstitute(
                    RAW_USER_ID, RAW_UA, "Linux", "Firefox", "Desktop", RAW_IP);
            UserLoginInformation info2 = factory.reconstitute(
                    RAW_USER_ID, RAW_UA, "Linux", "Firefox", "Desktop", "10.0.0.1");

            assertThat(info1).isNotEqualTo(info2);
        }
    }
}