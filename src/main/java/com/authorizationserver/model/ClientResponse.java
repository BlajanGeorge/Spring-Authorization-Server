package com.authorizationserver.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Set;

/**
 * Response entity to display a client
 *
 * @author Blajan George
 */
public record ClientResponse(@JsonProperty("client_id") String clientId,
                             @JsonProperty("client_name") String clientName,
                             Set<String> scopes,
                             @JsonProperty("client_authentication_methods") Set<String> clientAuthenticationMethods,
                             @JsonProperty("client_authorization_grant_types") Set<String> clientAuthorizationGrantTypes,
                             @JsonProperty("token_time_to_live") Integer tokenTimeToLive) {
}
