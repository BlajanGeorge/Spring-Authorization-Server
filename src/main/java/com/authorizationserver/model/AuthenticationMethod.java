package com.authorizationserver.model;

/**
 * Enum to define Authentication method for {@link org.springframework.security.oauth2.server.authorization.client.RegisteredClient}
 *
 * @author Blajan George
 */
public enum AuthenticationMethod {
    CLIENT_SECRET_BASIC("client_secret_basic"),
    CLIENT_SECRET_POST("client_secret_post");

    private final String authenticationMethodName;

    AuthenticationMethod(String authenticationMethodName) {
        this.authenticationMethodName = authenticationMethodName;
    }

    public String getAuthenticationMethodName() {
        return this.authenticationMethodName;
    }
}

