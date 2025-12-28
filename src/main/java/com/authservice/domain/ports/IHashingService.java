package com.authservice.domain.ports;

/**
 * Port for password hashing and verification.
 * <p>
 * Defines the contract for security operations related to password management.
 * Implementation (Adapter) will typically use BCrypt, Argon2, or Scrypt.
 */
public interface IHashingService {
    /**
     * Hashes a raw password.
     * 
     * @param password the raw password
     * 
     * @return the hashed password
     */
    String hash(String password);

    /**
     * Checks if a raw password matches a hashed value.
     * 
     * @param password       the raw password
     * 
     * @param hashedPassword the hashed password
     * @return true if matches, false otherwise
     */
    boolean verify(String password, String hashedPassword);

    /**
     * Checks if a string conforms to the expected hashing algorithm's format.
     * <p>
     * Use this during reconstitution to ensure stored values haven't been
     * tampered with or accidentally stored as plain-text.
     *
     * @param potentialHash the string to check
     * @return true if the string is formatted correctly for this algorithm
     */
    boolean isAlreadyHashed(String potentialHash);
}