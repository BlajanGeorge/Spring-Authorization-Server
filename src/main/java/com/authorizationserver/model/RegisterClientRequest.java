package com.authorizationserver.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

import java.util.List;

/**
 * Request entity to register a new client
 *
 * @author Blajan George
 */
public record RegisterClientRequest(@JsonProperty("client_id") @NotBlank(message = "client_id must not be blank.") String clientId,
                                    @JsonProperty("client_secret") @NotBlank(message = "client_secret must not be blank.") String clientSecret,
                                    @JsonProperty("client_name") String clientName,
                                    List<Scope> scopes,
                                    @JsonProperty("client_authentication_methods") List<AuthenticationMethod> clientAuthenticationMethods,
                                    @JsonProperty("client_authorization_grant_types") List<AuthorizationGrantType> clientAuthorizationGrantTypes,
                                    @JsonProperty("token_time_to_live") @Positive(message = "token_time_to_live must be positive.") @Max(value = 60, message = "token_time_to_live must be max 60 minutes.") Integer tokenTimeToLive) {
    @Override
    public String toString() {
        return "RegisterClientRequest{" + "clientId='" + clientId + '\'' + ", clientName='" + clientName + '\'' + ", scopes=" + scopes + ", authenticationMethod=" + clientAuthenticationMethods + ", authorizationGrantType=" + clientAuthorizationGrantTypes + ", tokenTimeToLive=" + tokenTimeToLive + '}';
    }
}
