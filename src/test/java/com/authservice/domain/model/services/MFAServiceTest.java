package com.authservice.domain.model.services;

import com.authservice.domain.model.entities.userLoginInformation.UserLoginInformation;
import com.authservice.domain.model.valueobjects.IPAddressValueObject;
import com.authservice.domain.model.valueobjects.UUIDValueObject;
import com.authservice.domain.model.valueobjects.UserAgentValueObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for the MFAService.
 * Updated to use UserAgentValueObject.reconstitute() to satisfy private
 * constructor constraints.
 */
class MFAServiceTest {

    private MFAService mfaService;
    private UUIDValueObject userId;
    private UserAgentValueObject chromeOnWindows;
    private IPAddressValueObject homeIp;

    @BeforeEach
    void setUp() {
        mfaService = new MFAService();
        userId = new UUIDValueObject(UUID.randomUUID().toString());

        // FIX: Use .reconstitute() because the constructor is now private
        chromeOnWindows = UserAgentValueObject.reconstitute(
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/120.0.0.0",
                "Windows",
                "Chrome",
                "Desktop");

        homeIp = new IPAddressValueObject("192.168.1.1");
    }

    @Test
    @DisplayName("Should require MFA when user has no login history (First Login)")
    void shouldRequireMFAForFirstLogin() {
        UserLoginInformation currentAttempt = new UserLoginInformation(userId, chromeOnWindows, homeIp);
        List<UserLoginInformation> emptyHistory = new ArrayList<>();

        boolean result = mfaService.isMFARequired(emptyHistory, currentAttempt);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Should NOT require MFA when current login matches an entry in history")
    void shouldNotRequireMFAForKnownContext() {
        UserLoginInformation pastLogin = new UserLoginInformation(userId, chromeOnWindows, homeIp);
        UserLoginInformation currentAttempt = new UserLoginInformation(userId, chromeOnWindows, homeIp);

        List<UserLoginInformation> history = List.of(pastLogin);

        // This works because equals() checks the internal fields of the VO
        boolean result = mfaService.isMFARequired(history, currentAttempt);

        assertThat(result).isFalse();
    }

    @Nested
    @DisplayName("Risk Detection Scenarios")
    class RiskScenarios {

        @Test
        @DisplayName("Should require MFA when IP address changes even if device is known")
        void shouldRequireMFAForNewIP() {
            UserLoginInformation knownLogin = new UserLoginInformation(userId, chromeOnWindows, homeIp);
            IPAddressValueObject coffeeShopIp = new IPAddressValueObject("10.0.0.50");

            UserLoginInformation newAttempt = new UserLoginInformation(userId, chromeOnWindows, coffeeShopIp);
            List<UserLoginInformation> history = List.of(knownLogin);

            boolean result = mfaService.isMFARequired(history, newAttempt);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should require MFA when device changes even if IP is known")
        void shouldRequireMFAForNewDevice() {
            UserLoginInformation knownLogin = new UserLoginInformation(userId, chromeOnWindows, homeIp);

            // FIX: Use .reconstitute() for the second device as well
            UserAgentValueObject iphoneSafari = UserAgentValueObject.reconstitute(
                    "Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X) Safari/605.1",
                    "iOS",
                    "Safari",
                    "Mobile");

            UserLoginInformation newAttempt = new UserLoginInformation(userId, iphoneSafari, homeIp);
            List<UserLoginInformation> history = List.of(knownLogin);

            boolean result = mfaService.isMFARequired(history, newAttempt);

            assertThat(result).isTrue();
        }
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException if current login is null")
    void shouldThrowExceptionForNullInput() {
        assertThatThrownBy(() -> mfaService.isMFARequired(new ArrayList<>(), null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Current login is null");
    }
}