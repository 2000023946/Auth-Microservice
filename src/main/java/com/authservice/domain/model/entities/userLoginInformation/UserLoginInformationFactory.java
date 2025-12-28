package com.authservice.domain.model.entities.userLoginInformation;

import com.authservice.domain.model.valueobjects.IPAddressValueObject;
import com.authservice.domain.model.valueobjects.UUIDValueObject;
import com.authservice.domain.model.valueobjects.UserAgentValueObject;
import com.authservice.domain.ports.IUserAgentParser;

/**
 * Factory responsible for creating {@link UserLoginInformation} instances.
 *
 * <p>
 * This factory coordinates the construction of login-related
 * Value Objects and delegates User-Agent parsing to a domain port.
 * It centralizes creation logic to keep the domain model consistent.
 * </p>
 */
public class UserLoginInformationFactory {

    private final IUserAgentParser uaParser;

    /**
     * Creates a new factory with the given User-Agent parser.
     *
     * @param uaParser
     *                 Port used to parse and decode raw User-Agent strings.
     */
    public UserLoginInformationFactory(IUserAgentParser uaParser) {
        this.uaParser = uaParser;
    }

    /**
     * Creates login information from a new authentication request.
     *
     * <p>
     * Intended for use in the Application Layer (e.g. Login use cases),
     * where raw input values are first received.
     * </p>
     *
     * @param rawUserId
     *                     Raw user identifier received from the request.
     *
     * @param rawUserAgent
     *                     Raw User-Agent header value.
     *
     * @param rawIpAddress
     *                     Raw IP address string.
     *
     * @return a fully initialized {@link UserLoginInformation} instance
     */
    public UserLoginInformation create(
            String rawUserId,
            String rawUserAgent,
            String rawIpAddress) {
        return new UserLoginInformation(
                new UUIDValueObject(rawUserId),
                UserAgentValueObject.create(rawUserAgent, uaParser),
                new IPAddressValueObject(rawIpAddress));
    }

    /**
     * Reconstitutes login information from persisted data.
     *
     * <p>
     * Intended for use in the Infrastructure layer when rebuilding
     * domain objects from storage.
     * </p>
     *
     * @param rawUserId
     *                  Persisted user identifier.
     *
     * @param rawUA
     *                  Original raw User-Agent string.
     *
     * @param os
     *                  Parsed operating system name.
     *
     * @param browser
     *                  Parsed browser name.
     *
     * @param device
     *                  Parsed device type.
     *
     * @param rawIp
     *                  Persisted IP address.
     *
     * @return a reconstructed {@link UserLoginInformation} instance
     */
    public UserLoginInformation reconstitute(
            String rawUserId,
            String rawUA,
            String os,
            String browser,
            String device,
            String rawIp) {
        return new UserLoginInformation(
                new UUIDValueObject(rawUserId),
                UserAgentValueObject.reconstitute(rawUA, os, browser, device),
                new IPAddressValueObject(rawIp));
    }
}
