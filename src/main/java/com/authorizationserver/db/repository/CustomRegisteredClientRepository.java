package com.authorizationserver.db.repository;

import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;

import java.util.List;

/**
 * Interface to extend current client repository capabilities
 *
 * @author Blajan George
 */
public interface CustomRegisteredClientRepository extends RegisteredClientRepository {

    /**
     * Delete a registered client entity by client id
     *
     * @param clientId client id
     */
    void deleteByClientId(String clientId);

    /**
     * Returns all registered clients
     *
     * @return all registered clients
     */
    List<RegisteredClient> getAllClients();
}
