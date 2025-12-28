package com.authservice.domain.ports;

import com.authservice.domain.model.valueobjects.UserAgentValueObject;

/**
 * Port for parsing raw User-Agent strings into structured Value Objects.
 */
public interface IUserAgentParser {
    /**
     * 
     * @param rawUserAgent The raw string we want to parse
     * @return the User Agent that has the information
     */
    UserAgentValueObject parse(String rawUserAgent);
}