package com.authorizationserver.model;

/**
 * Enum to define Authorization grant type for {@link org.springframework.security.oauth2.server.authorization.client.RegisteredClient}
 *
 * @author Blajan George
 */
public enum AuthorizationGrantType {
    CLIENT_CREDENTIALS("client_credentials");

    private final String authorizationGrantTypeName;

    AuthorizationGrantType(String authorizationGrantTypeName) {
        this.authorizationGrantTypeName = authorizationGrantTypeName;
    }

    public String getAuthorizationGrantTypeName() {
        return this.authorizationGrantTypeName;
    }
}
