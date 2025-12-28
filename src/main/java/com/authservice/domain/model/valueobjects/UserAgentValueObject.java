package com.authservice.domain.model.valueobjects;

import com.authservice.domain.ports.IUserAgentParser;
import java.util.Objects;

/**
 * Value Object representing a parsed User-Agent string.
 * <p>
 * This class encapsulates structured data (OS, Browser, Device) extracted from
 * raw headers to enable security risk analysis and session tracking. It uses
 * static factory methods to distinguish between fresh parsing and persistence
 * reconstitution.
 */
public class UserAgentValueObject extends ValueObject<String> {

    private final String rawValue;
    private final String os;
    private final String browser;
    private final String device;

    /**
     * Private constructor to enforce the use of static factory methods.
     * Ensures all fields are handled consistently.
     */
    private UserAgentValueObject(String rawValue, String os, String browser, String device) {
        if (rawValue == null || rawValue.isBlank()) {
            throw new IllegalArgumentException("Raw User Agent string cannot be null or blank");
        }
        this.rawValue = rawValue;
        this.os = Objects.requireNonNullElse(os, "Unknown");
        this.browser = Objects.requireNonNullElse(browser, "Unknown");
        this.device = Objects.requireNonNullElse(device, "Unknown");
    }

    /**
     * Factory method to create a UserAgentValueObject by parsing a raw header.
     * Use this when receiving a new request from the Infrastructure/Web layer.
     *
     * @param rawUserAgent The raw User-Agent header string.
     * @param parser       The port used to perform the parsing logic.
     * @return A newly parsed UserAgentValueObject.
     */
    public static UserAgentValueObject create(String rawUserAgent, IUserAgentParser parser) {
        if (rawUserAgent == null || rawUserAgent.isBlank()) {
            throw new IllegalArgumentException("Raw User Agent cannot be blank");
        }
        if (parser == null) {
            throw new IllegalArgumentException("Parser cannot be null");
        }

        // Call the parser
        UserAgentValueObject parsed = parser.parse(rawUserAgent);

        // CRITICAL FIX: Check if the parser failed to return an object
        if (parsed == null) {
            throw new IllegalArgumentException("Failed to parse the provided User Agent string");
        }

        return new UserAgentValueObject(
                rawUserAgent,
                parsed.getOs(),
                parsed.getBrowser(),
                parsed.getDevice());
    }

    /**
     * Factory method to reconstitute a UserAgentValueObject from persistence.
     * Use this in Repositories/Mappers when the data is already structured in SQL.
     *
     * @param raw     The original raw header string.
     * @param os      The stored OS value.
     * @param browser The stored browser value.
     * @param device  The stored device value.
     * @return A reconstituted UserAgentValueObject.
     */
    public static UserAgentValueObject reconstitute(String raw, String os, String browser, String device) {
        return new UserAgentValueObject(raw, os, browser, device);
    }

    /**
     * Returns a concatenated representation of the structured components.
     * 
     * @return String in format "OS Browser Device"
     */
    @Override
    public String getValue() {
        return os + " " + browser + " " + device;
    }

    /**
     * Returns the original raw User-Agent string.
     * Required for security auditing and historical mapping.
     * 
     * @return raw header value
     */
    public String getRawValue() {
        return rawValue;
    }

    /**
     * @return Identified Operating System
     */
    public String getOs() {
        return os;
    }

    /**
     * @return Identified Browser
     */
    public String getBrowser() {
        return browser;
    }

    /**
     * @return Identified Device type (e.g., Mobile, Desktop)
     */
    public String getDevice() {
        return device;
    }

    /**
     * Creates a unique hex fingerprint based on the raw value.
     * Optimized for pinning sessions in Redis to save memory.
     * 
     * @return Hexadecimal string representation of the raw value's hash
     */
    public String getFingerprint() {
        return Integer.toHexString(rawValue.hashCode());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UserAgentValueObject that = (UserAgentValueObject) o;
        return Objects.equals(rawValue, that.rawValue) &&
                Objects.equals(os, that.os) &&
                Objects.equals(browser, that.browser) &&
                Objects.equals(device, that.device);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rawValue, os, browser, device);
    }
}