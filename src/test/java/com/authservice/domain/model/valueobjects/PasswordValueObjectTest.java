package com.authservice.domain.model.valueobjects;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PasswordValueObjectTest {

    @Test
    @DisplayName("Should create PasswordValueObject when password is complex")
    void shouldCreateWhenComplex() {
        String complex = "SecureP@ss123!";
        PasswordValueObject password = new PasswordValueObject(complex);
        assertThat(password.getValue()).isEqualTo(complex);
    }

    @Nested
    @DisplayName("Complexity Validation Failures")
    class ComplexityFailures {

        @ParameterizedTest
        @ValueSource(strings = {
                "short1!", // Too short
                "nouppercase1!", // Missing uppercase
                "NOLOWERCASE1!", // Missing lowercase
                "NoSpecialChar1", // Missing special character
                "NoNumber@@@@" // Missing number
        })
        @DisplayName("Should throw complexity error for weak passwords")
        void shouldThrowComplexityError(String weakPassword) {
            assertThatThrownBy(() -> new PasswordValueObject(weakPassword))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Password must be at least 8 characters long");
        }

        @ParameterizedTest
        @ValueSource(strings = { "", "   " })
        @DisplayName("Should throw empty error for blank passwords")
        void shouldThrowEmptyError(String blankPassword) {
            assertThatThrownBy(() -> new PasswordValueObject(blankPassword))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Password cannot be empty");
        }
    }
}