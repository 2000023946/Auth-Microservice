package com.authservice.domain.model.valueobjects;

import com.authservice.domain.ports.IUserAgentParser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * Unit tests for UserAgentValueObject.
 * Verifies the Static Factory patterns for creation and reconstitution.
 */
class UserAgentValueObjectTest {

    private static final String RAW_UA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/120.0.0.0";
    private static final String OS = "Windows";
    private static final String BROWSER = "Chrome";
    private static final String DEVICE = "Desktop";

    @Nested
    @DisplayName("Factory Creation Tests (.create)")
    class CreationTests {

        @Test
        @DisplayName("Should create via parser with correct components")
        void shouldCreateViaParser() {
            // Arrange
            IUserAgentParser mockParser = Mockito.mock(IUserAgentParser.class);
            // We use reconstitute to create the return value for the mock
            UserAgentValueObject mockResult = UserAgentValueObject.reconstitute(RAW_UA, OS, BROWSER, DEVICE);

            when(mockParser.parse(RAW_UA)).thenReturn(mockResult);

            // Act
            UserAgentValueObject ua = UserAgentValueObject.create(RAW_UA, mockParser);

            // Assert
            assertThat(ua.getRawValue()).isEqualTo(RAW_UA);
            assertThat(ua.getOs()).isEqualTo(OS);
            assertThat(ua.getBrowser()).isEqualTo(BROWSER);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = { "  " })
        @DisplayName("Should throw exception for invalid raw value during creation")
        void shouldThrowExceptionForInvalidRawValue(String invalidRawValue) {
            IUserAgentParser mockParser = Mockito.mock(IUserAgentParser.class);

            assertThatThrownBy(() -> UserAgentValueObject.create(invalidRawValue, mockParser))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Raw User Agent cannot be blank");
        }
    }

    @Nested
    @DisplayName("Reconstitution Tests (.reconstitute)")
    class ReconstitutionTests {

        @Test
        @DisplayName("Should map fields directly from database values")
        void shouldReconstituteCorrectly() {
            UserAgentValueObject ua = UserAgentValueObject.reconstitute(RAW_UA, OS, BROWSER, DEVICE);

            assertThat(ua.getRawValue()).isEqualTo(RAW_UA);
            assertThat(ua.getOs()).isEqualTo(OS);
            assertThat(ua.getBrowser()).isEqualTo(BROWSER);
            assertThat(ua.getDevice()).isEqualTo(DEVICE);
        }

        @Test
        @DisplayName("Should handle 'Unknown' defaults via private constructor logic")
        void shouldHandleNullComponentsInReconstitution() {
            UserAgentValueObject ua = UserAgentValueObject.reconstitute(RAW_UA, null, null, null);

            assertThat(ua.getOs()).isEqualTo("Unknown");
            assertThat(ua.getBrowser()).isEqualTo("Unknown");
            assertThat(ua.getDevice()).isEqualTo("Unknown");
        }
    }

    @Nested
    @DisplayName("Behavioral Logic Tests")
    class BehavioralTests {

        @Test
        @DisplayName("getValue should return structured component string")
        void getValueShouldReturnFormattedString() {
            UserAgentValueObject ua = UserAgentValueObject.reconstitute(RAW_UA, OS, BROWSER, DEVICE);
            // Note: Updated to match your specific implementation (space-separated or
            // concatenated)
            assertThat(ua.getValue()).isEqualTo(OS + " " + BROWSER + " " + DEVICE);
        }

        @Test
        @DisplayName("getFingerprint should be a consistent hex hash of the raw UA")
        void getFingerprintShouldBeConsistent() {
            UserAgentValueObject ua = UserAgentValueObject.reconstitute(RAW_UA, OS, BROWSER, DEVICE);
            String expected = Integer.toHexString(RAW_UA.hashCode());

            assertThat(ua.getFingerprint()).isEqualTo(expected);
        }
    }

    @Nested
    @DisplayName("Equality and HashCode")
    class EqualityTests {

        @Test
        @DisplayName("Objects with identical data should be equal")
        void equalityTest() {
            UserAgentValueObject ua1 = UserAgentValueObject.reconstitute(RAW_UA, OS, BROWSER, DEVICE);
            UserAgentValueObject ua2 = UserAgentValueObject.reconstitute(RAW_UA, OS, BROWSER, DEVICE);

            assertThat(ua1).isEqualTo(ua2);
            assertThat(ua1.hashCode()).isEqualTo(ua2.hashCode());
        }
    }
}