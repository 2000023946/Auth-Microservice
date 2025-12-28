package com.authservice.domain.model.valueobjects;

import com.authservice.domain.ports.IHashingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@DisplayName("PasswordHashValueObject Unit Tests")
class PasswordHashValueObjectTest {

    private IHashingService mockHasher;
    private final String RAW_PASS = "SecureP@ss123";
    private final String VALID_HASH = "$2a$12$ValidHashExampleStringForBcrypt";

    @BeforeEach
    void setUp() {
        mockHasher = Mockito.mock(IHashingService.class);
    }

    @Nested
    @DisplayName("Creation Tests")
    class CreationTests {

        @Test
        @DisplayName("Should successfully hash and create VO from raw password")
        void shouldCreateHashFromRawPassword() {
            // Arrange
            PasswordValueObject passwordVO = new PasswordValueObject(RAW_PASS);
            when(mockHasher.hash(RAW_PASS)).thenReturn(VALID_HASH);

            // Act
            PasswordHashValueObject result = PasswordHashValueObject.create(passwordVO, mockHasher);

            // Assert
            assertThat(result.getValue()).isEqualTo(VALID_HASH);
            verify(mockHasher, times(1)).hash(RAW_PASS);
        }

        @Test
        @DisplayName("Should throw exception if hashing service returns null or blank")
        void shouldThrowIfHashingFails() {
            PasswordValueObject passwordVO = new PasswordValueObject(RAW_PASS);
            when(mockHasher.hash(RAW_PASS)).thenReturn("");

            assertThatThrownBy(() -> PasswordHashValueObject.create(passwordVO, mockHasher))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("failed to generate a valid hash");
        }
    }

    @Nested
    @DisplayName("Reconstitution Tests")
    class ReconstitutionTests {

        @Test
        @DisplayName("Should successfully reconstitute when string matches hash format")
        void shouldReconstituteValidHash() {
            // Arrange
            when(mockHasher.isAlreadyHashed(VALID_HASH)).thenReturn(true);

            // Act
            PasswordHashValueObject result = PasswordHashValueObject.reconstitute(VALID_HASH, mockHasher);

            // Assert
            assertThat(result.getValue()).isEqualTo(VALID_HASH);
            verify(mockHasher, times(1)).isAlreadyHashed(VALID_HASH);
        }

        @Test
        @DisplayName("Should throw IllegalStateException when DB returns plain-text or invalid hash")
        void shouldThrowOnInvalidHashFormat() {
            // Arrange
            String plainText = "i_am_not_a_hash";
            when(mockHasher.isAlreadyHashed(plainText)).thenReturn(false);

            // Act & Assert
            assertThatThrownBy(() -> PasswordHashValueObject.reconstitute(plainText, mockHasher))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Possible data corruption or plain-text leak detected");
        }
    }

    @Nested
    @DisplayName("Null Safety Tests")
    class NullSafetyTests {

        @Test
        @DisplayName("Should throw exception if HashingService is null")
        void shouldThrowIfServiceIsNull() {
            PasswordValueObject passwordVO = new PasswordValueObject(RAW_PASS);

            assertThatThrownBy(() -> PasswordHashValueObject.create(passwordVO, null))
                    .isInstanceOf(IllegalArgumentException.class);

            assertThatThrownBy(() -> PasswordHashValueObject.reconstitute(VALID_HASH, null))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }
}