package com.authorizationserver.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.Set;

/**
 * Entity to display metadata of auth service
 *
 * @author Blajan George
 */
@Builder
public record MetadataResponse(String issuer,
                               @JsonProperty("token_endpoint") String tokenEndpoint,
                               @JsonProperty("jwk_set_endpoint") String jwkSetEndpoint,
                               @JsonProperty("register_client_endpoint") String registerClientEndpoint,
                               @JsonProperty("patch_client_endpoint") String patchClientEndpoint,
                               @JsonProperty("delete_client_endpoint") String deleteClientEndpoint,
                               @JsonProperty("get_client_endpoint") String getClientEndpoint,
                               @JsonProperty("scopes_supported") Set<String> scopesSupported,
                               @JsonProperty("token_endpoint_auth_signing_alg_values_supported") Set<String> authSignValuesSupported,
                               @JsonProperty("token_endpoint_auth_methods_supported") Set<String> authMethodsSupported,
                               @JsonProperty("auth_grant_types_supported") Set<String> authGrantTypesSupported) {
}
