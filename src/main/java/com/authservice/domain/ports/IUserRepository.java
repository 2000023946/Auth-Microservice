package com.authservice.domain.ports;

import com.authservice.domain.model.aggregates.user.UserAggregate;
import com.authservice.domain.model.valueobjects.EmailValueObject;
import com.authservice.domain.model.valueobjects.UUIDValueObject;
import java.util.Optional;

/**
 * Port defining the contract for User Aggregate persistence and retrieval.
 * <p>
 * This interface is a "Gatekeeper" to the infrastructure layer. By using
 * Domain Value Objects as parameters, it ensures the persistence layer
 * never processes malformed or unvalidated data.
 * </p>
 */
public interface IUserRepository {

    /**
     * Checks if a user identity already exists for the given email address.
     * <p>
     * Primarily used during registration to enforce the uniqueness invariant
     * of user identities.
     * </p>
     *
     * @param email the validated and normalized EmailValueObject to check
     * @return true if a user already exists with this email; false otherwise
     */
    boolean existsByEmail(EmailValueObject email);

    /**
     * Retrieves a user aggregate based on their unique identifier.
     *
     * @param userId the UUIDValueObject representing the user's unique identity
     * @return an Optional containing the UserAggregate if found, or empty if not
     */
    Optional<UserAggregate> findById(UUIDValueObject userId);

    /**
     * Retrieves a user aggregate based on their validated email address.
     * <p>
     * Used during the authentication flow to fetch the user's password hash
     * and security state (like failed login attempts).
     * </p>
     *
     * @param email the validated EmailValueObject to search for
     * @return an Optional containing the UserAggregate if found, or empty if not
     */
    Optional<UserAggregate> findByEmail(EmailValueObject email);

    /**
     * Persists the current state of a User Aggregate.
     * <p>
     * This method handles both the creation of new users and the updating
     * of existing ones (e.g., updating failed login counters or verification
     * status).
     * It ensures the entire "Domain Capsule" state is saved atomically.
     * </p>
     *
     * @param user the UserAggregate instance to be saved
     */
    void save(UserAggregate user);
}