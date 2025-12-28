package com.authservice.domain.model.services.credentialService.registration;

import com.authservice.domain.ports.IUserRepository;
import com.authservice.domain.model.valueobjects.EmailValueObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@DisplayName("Registration Validation Service - Invariant Tests")
class RegistrationValidationServiceTest {

    private IUserRepository userRepository;
    private RegistrationValidationService registrationService;

    private final String VALID_EMAIL = "mohamed@gatech.edu";
    private final String VALID_PASS = "SecurePass123!.";

    @BeforeEach
    void setUp() {
        userRepository = Mockito.mock(IUserRepository.class);
        registrationService = new RegistrationValidationService(userRepository);
    }

    @Test
    @DisplayName("Structural Gate: Should throw exception for invalid email format")
    void shouldFailForBadEmail() {
        // Step 1: Structural check happens before repository is even touched
        assertThatThrownBy(() -> registrationService.validateForRegistration("not-an-email", VALID_PASS))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid email format");
    }

    @Test
    @DisplayName("Structural Gate: Should throw exception if password is too similar to email")
    void shouldFailForSimilarPassword() {
        assertThatThrownBy(() -> registrationService.validateForRegistration("mohamed@gatech.edu", "moHAmed123!."))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("too similar");
    }

    @Test
    @DisplayName("Business Gate: Should throw exception if email is already taken")
    void shouldFailIfEmailExists() {
        // Arrange: Repository says user exists
        when(userRepository.existsByEmail(any(EmailValueObject.class))).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> registrationService.validateForRegistration(VALID_EMAIL, VALID_PASS))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email already exists");
    }

    @Test
    @DisplayName("Certificate Issuance: Should return RegistrationProof when all gates are passed")
    void shouldIssueProofOnSuccess() {
        // Arrange: Repository says user does NOT exist
        when(userRepository.existsByEmail(any(EmailValueObject.class))).thenReturn(false);

        // Act
        RegistrationProof proof = registrationService.validateForRegistration(VALID_EMAIL, VALID_PASS);

        // Assert
        assertThat(proof).isNotNull();
        assertThat(proof.getEmail().getValue()).isEqualTo(VALID_EMAIL);
        // Note: We don't verify password equality here if it's sensitive,
        // but we verify the proof was issued.
        assertThat(proof.getPassword()).isNotNull();
    }
}