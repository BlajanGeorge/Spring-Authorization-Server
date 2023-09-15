package com.authorizationserver.db.repository;

import com.authorizationserver.db.model.Oauth2Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for accessing and manipulating {@link Oauth2Client} data
 *
 * @author Blajan George
 */
@Repository
public interface Oauth2ClientRepository extends JpaRepository<Oauth2Client, String> {
    /**
     * Find an oauth2 client by client id
     *
     * @param clientId client id
     * @return {@link Optional}
     */
    Optional<Oauth2Client> findByClientId(String clientId);

    /**
     * Delete an oauth2 client by client id
     *
     * @param clientId client id
     */
    void deleteOauth2ClientByClientId(String clientId);
}
