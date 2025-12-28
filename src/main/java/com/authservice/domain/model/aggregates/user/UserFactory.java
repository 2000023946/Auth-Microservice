package com.authservice.domain.model.aggregates.user;

import com.authservice.domain.model.services.credentialService.registration.RegistrationProof;
import com.authservice.domain.model.valueobjects.EmailValueObject;
import com.authservice.domain.model.valueobjects.PasswordHashValueObject;
import com.authservice.domain.model.valueobjects.UUIDValueObject;
import com.authservice.domain.model.valueobjects.PasswordValueObject;
import com.authservice.domain.ports.IHashingService;
import com.authservice.domain.ports.UserRepositoryProof;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Factory responsible for the creation and reconstitution of UserAggregates.
 * <p>
 * Translates raw infrastructure inputs (Strings from DB or JSON) into validated
 * Domain Value Objects and Aggregates.
 */
public class UserFactory {

    private final IHashingService hashingService;

    /**
     * @param hashingService the hashing port used for password transformations and
     *                       validation
     */
    public UserFactory(IHashingService hashingService) {
        this.hashingService = hashingService;
    }

    /**
     * Creates a brand new UserAggregate from raw registration inputs.
     *
     * @param proof the certicate issued only by the registration service
     *              that prooves the credentials are valid
     * @return Fully initialized UserAggregate
     */
    public UserAggregate create(RegistrationProof proof) {

        EmailValueObject emailVO = proof.getEmail();
        PasswordValueObject passwordVO = proof.getPassword();

        // Hashing orchestrated here for new users
        PasswordHashValueObject hashVO = PasswordHashValueObject.create(passwordVO, hashingService);

        UUIDValueObject userId = new UUIDValueObject(UUID.randomUUID().toString());

        return new UserAggregate(userId, emailVO, hashVO);
    }

    /**
     * Reconstitutes an EXISTING UserAggregate using a repository certificate.
     * 
     * @param repoProof The carrier containing all raw persistence data.
     * 
     * @return Fully reanimated UserAggregate.
     */
    public UserAggregate reconstitute(UserRepositoryProof repoProof) {
        // 1. Convert Identity and Email
        UUIDValueObject userIdVO = new UUIDValueObject(repoProof.getRawUserId());
        EmailValueObject emailVO = new EmailValueObject(repoProof.getRawEmail());

        // 2. Load the stored hash (Validates format via the hashing service)
        PasswordHashValueObject hashVO = PasswordHashValueObject.reconstitute(
                repoProof.getRawHash(),
                hashingService);

        // 3. Parse Domain Timestamps
        LocalDateTime created = LocalDateTime.parse(repoProof.getRawCreated());
        LocalDateTime updated = LocalDateTime.parse(repoProof.getRawUpdated());
        LocalDateTime lastReset = (repoProof.getRawLastReset() != null)
                ? LocalDateTime.parse(repoProof.getRawLastReset())
                : null;

        return new UserAggregate(
                userIdVO,
                emailVO,
                hashVO,
                repoProof.isVerified(),
                repoProof.getFailedAttempts(),
                created,
                updated,
                lastReset);
    }
}