package com.authservice.domain.model.valueobjects;

import java.util.regex.Pattern;

/**
 * IPAddressValueObject represents an immutable IPv4 or IPv6 address within the
 * domain.
 * <p>
 * This value object encapsulates all validation, normalization, and comparison
 * rules related to an IP address. Two IPAddressValueObject instances are
 * considered equal if their underlying IP values are equal.
 * <p>
 * This class is intended to be immutable and compared by value, not by
 * identity.
 */
public class IPAddressValueObject extends ValueObject<String> {

    /**
     * The underlying IP address value.
     */
    private final String value;

    /**
     * Regex pattern for validating IPv4 addresses.
     */
    private static final Pattern IPV4_PATTERN = Pattern.compile(
            "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}" +
                    "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$");

    /**
     * Regex pattern for validating IPv6 addresses (simple, expanded form).
     */
    private static final Pattern IPV6_PATTERN = Pattern.compile(
            "^(?:[0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$");

    /**
     * Constructs a new IPAddressValueObject after validating the IP address.
     *
     * @param ipAddress the IP address string to assign
     * @throws IllegalArgumentException if the IP address is null or invalid
     */
    public IPAddressValueObject(String ipAddress) {
        if (ipAddress == null) {
            throw new IllegalArgumentException("IP address cannot be null!");
        }
        String normalized = ipAddress.trim();
        if (!IPV4_PATTERN.matcher(normalized).matches() &&
                !IPV6_PATTERN.matcher(normalized).matches()) {
            throw new IllegalArgumentException("Invalid IP address format: " + ipAddress);
        }
        this.value = normalized;
    }

    /**
     * Returns the normalized IP address value.
     *
     * @return the IP address string
     */
    @Override
    public String getValue() {
        return value;
    }

}
