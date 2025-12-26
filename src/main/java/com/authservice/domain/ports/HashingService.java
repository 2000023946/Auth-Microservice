package com.authservice.domain.ports;

public interface HashingService {
    /**
     * Hashes a raw password.
     * 
     * @param password the raw password
     * @return the hashed password
     */
    String hash(String password);

    /**
     * Checks if a raw password matches a hashed value.
     * 
     * @param password       the raw password
     * @param hashedPassword the hashed password
     * @return true if matches, false otherwise
     */
    boolean verify(String password, String hashedPassword);
}
