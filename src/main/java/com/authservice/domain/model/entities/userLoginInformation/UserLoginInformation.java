package com.authservice.domain.model.entities.userLoginInformation;

import java.util.Objects;

import com.authservice.domain.model.valueobjects.IPAddressValueObject;
import com.authservice.domain.model.valueobjects.UUIDValueObject;
import com.authservice.domain.model.valueobjects.UserAgentValueObject;

/**
 * Domain Entity representing a unique login context for a user.
 * <p>
 * This entity is used to track known devices and locations to perform risk
 * analysis.
 * It is immutable by design as it represents a historical security state.
 */
public class UserLoginInformation {

    /** The unique identifier of the user this information belongs to. */
    private final UUIDValueObject userId;

    /** The value object encapsulating parsed browser, OS, and device data. */
    private final UserAgentValueObject userAgent;

    /** The value object encapsulating the IP address used during the login. */
    private final IPAddressValueObject ipAddress;

    /**
     * Constructs a new UserLoginInformation entity.
     *
     * @param userId    the unique identifier of the user
     * @param userAgent the value object containing parsed user agent details
     * @param ipAddress the value object containing the validated IP address
     */
    public UserLoginInformation(UUIDValueObject userId, UserAgentValueObject userAgent,
            IPAddressValueObject ipAddress) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        if (userAgent == null) {
            throw new IllegalArgumentException("User Agent cannot be null");
        }
        if (ipAddress == null) {
            throw new IllegalArgumentException("IP Address cannot be null");
        }
        this.userId = userId;
        this.userAgent = userAgent;
        this.ipAddress = ipAddress;
    }

    /**
     * Gets the unique identifier of the user.
     *
     * @return the UUIDValueObject of the user
     */
    public UUIDValueObject getUserId() {
        return userId;
    }

    /**
     * Gets the full UserAgentValueObject.
     *
     * @return the UserAgentValueObject containing raw and parsed data
     */
    public UserAgentValueObject getUserAgent() {
        return userAgent;
    }

    /**
     * Gets the IPAddressValueObject.
     *
     * @return the IPAddressValueObject representing the login location
     */
    public IPAddressValueObject getIpAddress() {
        return ipAddress;
    }

    /**
     * Helper method to retrieve the Operating System from the nested
     * UserAgentValueObject.
     *
     * @return the OS string (e.g., "Windows", "iOS")
     */
    public String getOs() {
        return userAgent.getOs();
    }

    /**
     * Helper method to retrieve the Browser from the nested UserAgentValueObject.
     *
     * @return the Browser string (e.g., "Chrome", "Safari")
     */
    public String getBrowser() {
        return userAgent.getBrowser();
    }

    /**
     * Helper method to retrieve the Device from the nested UserAgentValueObject.
     *
     * @return the Device type (e.g., "Desktop", "Mobile")
     */
    public String getDevice() {
        return userAgent.getDevice();
    }

    /**
     * Compares this login information with another object for equality.
     * * @param o the reference object with which to compare.
     * 
     * @return true if this object is the same as the obj argument; false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof UserLoginInformation)) {
            return false;
        }
        UserLoginInformation that = (UserLoginInformation) o;
        return Objects.equals(userId, that.userId) &&
                Objects.equals(userAgent, that.userAgent) &&
                Objects.equals(ipAddress, that.ipAddress);
    }

    /**
     * Returns a hash code value for the object.
     *
     * @return a hash code value for this object.
     */
    @Override
    public int hashCode() {
        return Objects.hash(userId, userAgent, ipAddress);
    }
}