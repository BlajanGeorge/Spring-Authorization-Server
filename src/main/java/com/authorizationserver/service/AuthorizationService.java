package com.authorizationserver.service;

import com.authorizationserver.model.MetadataResponse;
import com.authorizationserver.model.RegisterClientRequest;
import com.authorizationserver.model.ClientResponse;
import com.authorizationserver.model.PatchClientRequest;

import java.util.List;

/**
 * Authorization service interface to define API contract
 *
 * @author Blajan George
 */
public interface AuthorizationService {
    /**
     * Method to register a new Oauth2 client
     *
     * @param registerClientRequest {@link RegisterClientRequest} request entity for registering client
     * @param authorizationSecret   authorization secret to confirm identity
     */
    void registerOauth2Client(final RegisterClientRequest registerClientRequest, final String authorizationSecret);

    /**
     * Method to patch an Oauth2 client
     *
     * @param patchClientRequest  {@link PatchClientRequest} request entity for patch client
     * @param authorizationSecret authorization secret to confirm identity
     * @param clientId            client id
     */
    void patchOauth2Client(final PatchClientRequest patchClientRequest, final String authorizationSecret, final String clientId);

    /**
     * Method to delete an Oauth2 client
     *
     * @param authorizationSecret authorization secret to confirm identity
     * @param clientId            client id
     */
    void deleteOauth2Client(final String authorizationSecret, final String clientId);

    /**
     * Method to retrieve client by id
     *
     * @param authorizationSecret authorization secret to confirm identity
     * @param clientId            client id
     * @return {@link ClientResponse}
     */
    ClientResponse getOauth2Client(final String authorizationSecret, final String clientId);

    /**
     * Method to retrieve all clients
     *
     * @param authorizationSecret authorization secret to confirm identity
     * @return {@link ClientResponse}
     */
    List<ClientResponse> getOauth2Clients(final String authorizationSecret);

    /**
     * Fetch metadata of auth service
     *
     * @return {@link MetadataResponse}
     */
    MetadataResponse getMetadata();
}
