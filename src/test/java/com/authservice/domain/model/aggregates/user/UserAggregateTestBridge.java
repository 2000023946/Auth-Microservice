package com.authservice.domain.model.aggregates.user;

import com.authservice.domain.model.valueobjects.EmailValueObject;
import com.authservice.domain.model.valueobjects.PasswordHashValueObject;
import com.authservice.domain.model.valueobjects.UUIDValueObject;

/**
 * Test bridge for {@link UserAggregate}.
 * <p>
 * This class resides in the same package as {@code UserAggregate} to access
 * its package-private constructor for testing purposes. It allows test code
 * to create instances of {@code UserAggregate} without exposing the constructor
 * publicly in production code.
 * </p>
 */
public class UserAggregateTestBridge {

    /**
     * Creates a {@link UserAggregate} instance for testing.
     * <p>
     * This method is intended for use in tests to simulate a user entity with
     * specific identifiers, email, and password hash, enabling controlled
     * testing of aggregate behavior.
     * </p>
     *
     * @param id    the UUID value object representing the user's ID
     * @param email the email value object of the user
     * @param hash  the password hash value object of the user
     * @return a new {@link UserAggregate} instance for testing
     */
    public static UserAggregate create(
            UUIDValueObject id,
            EmailValueObject email,
            PasswordHashValueObject hash) {
        return new UserAggregate(id, email, hash);
    }
}
