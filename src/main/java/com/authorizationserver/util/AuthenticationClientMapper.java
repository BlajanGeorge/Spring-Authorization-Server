package com.authorizationserver.util;

import com.authorizationserver.model.RegisterClientRequest;
import com.authorizationserver.model.AuthenticationMethod;
import com.authorizationserver.model.ClientResponse;
import com.authorizationserver.model.Scope;
import lombok.experimental.UtilityClass;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.authorizationserver.constants.Constants.DEFAULT_TOKEN_AVAILABILITY_IN_MINUTES;

/**
 * Utility class for mapping
 *
 * @author Blajan George
 */
@UtilityClass
public class AuthenticationClientMapper {

    /**
     * Map {@link RegisterClientRequest} to {@link RegisteredClient}
     *
     * @param registerClientRequest Request entity
     * @return {@link RegisteredClient}
     */
    @SuppressWarnings("java:S3776")
    public static RegisteredClient map(final RegisterClientRequest registerClientRequest, final BCryptPasswordEncoder bCryptPasswordEncoder) {
        RegisteredClient.Builder registerClientBuilder = RegisteredClient.withId(UUID.randomUUID().toString()).clientId(registerClientRequest.clientId()).clientSecret(bCryptPasswordEncoder.encode(registerClientRequest.clientSecret()));

        final String clientName = registerClientRequest.clientName();
        final List<Scope> scopes = registerClientRequest.scopes();
        final List<AuthenticationMethod> clientAuthenticationMethods = registerClientRequest.clientAuthenticationMethods();
        final List<com.authorizationserver.model.AuthorizationGrantType> clientAuthorizationGrantTypes = registerClientRequest.clientAuthorizationGrantTypes();
        final Integer tokenTimeToLive = registerClientRequest.tokenTimeToLive();

        if (clientName != null) {
            registerClientBuilder.clientName(clientName);
        }

        if (scopes != null && !scopes.isEmpty()) {
            for (Scope scope : scopes) {
                registerClientBuilder.scope(scope.name());
            }
        } else {
            registerClientBuilder.scope(Scope.ALL.name());
        }

        if (clientAuthenticationMethods != null && !clientAuthenticationMethods.isEmpty()) {
            for (AuthenticationMethod clientAuthenticationMethod : clientAuthenticationMethods) {
                registerClientBuilder.clientAuthenticationMethod(new ClientAuthenticationMethod(clientAuthenticationMethod.getAuthenticationMethodName()));
            }
        } else {
            registerClientBuilder.clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC);
        }

        if (clientAuthorizationGrantTypes != null && !clientAuthorizationGrantTypes.isEmpty()) {
            for (com.authorizationserver.model.AuthorizationGrantType authorizationGrantType : clientAuthorizationGrantTypes) {
                registerClientBuilder.authorizationGrantType(new AuthorizationGrantType(authorizationGrantType.getAuthorizationGrantTypeName()));
            }
        } else {
            registerClientBuilder.authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS);
        }

        registerClientBuilder.tokenSettings(TokenSettings.builder().accessTokenTimeToLive(Duration.ofMinutes(Objects.requireNonNullElse(tokenTimeToLive, DEFAULT_TOKEN_AVAILABILITY_IN_MINUTES))).build());

        return registerClientBuilder.build();
    }

    /**
     * Map {@link RegisteredClient} to {@link ClientResponse}
     *
     * @param registeredClient Db entity
     * @return {@link ClientResponse}
     */
    public static ClientResponse map(final RegisteredClient registeredClient) {
        return new ClientResponse(
                registeredClient.getClientId(),
                registeredClient.getClientName(),
                registeredClient.getScopes(),
                registeredClient.getClientAuthenticationMethods().stream().map(ClientAuthenticationMethod::getValue).map(String::toUpperCase).collect(Collectors.toSet()),
                registeredClient.getAuthorizationGrantTypes().stream().map(AuthorizationGrantType::getValue).map(String::toUpperCase).collect(Collectors.toSet()),
                (int) registeredClient.getTokenSettings().getAccessTokenTimeToLive().toMinutes());
    }
}

