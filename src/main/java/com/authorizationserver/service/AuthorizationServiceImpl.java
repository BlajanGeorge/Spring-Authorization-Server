package com.authorizationserver.service;

import com.authorizationserver.db.repository.CustomRegisteredClientRepository;
import com.authorizationserver.exception.EntityNotFoundException;
import com.authorizationserver.model.*;
import com.authorizationserver.util.AuthenticationClientMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.authorizationserver.constants.Constants.*;

/**
 * Authorization Service implementation
 *
 * @author Blajan George
 */
@Slf4j
@Service
public class AuthorizationServiceImpl implements AuthorizationService {
    /**
     * Secret to validate identity for client manipulation endpoints
     */
    @Value("${authorizationSecret}")
    private String authorizationSecret;
    /**
     * Repository that provide access to db operations
     */
    private final CustomRegisteredClientRepository clientRepository;

    /**
     * Used for hashing client secrets
     */
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    /**
     * Metadata context object class
     */
    private final MetadataContext metadataContext;

    public AuthorizationServiceImpl(final CustomRegisteredClientRepository clientRepository,
                                    final BCryptPasswordEncoder bCryptPasswordEncoder,
                                    final MetadataContext metadataContext) {
        this.clientRepository = clientRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.metadataContext = metadataContext;
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public void registerOauth2Client(RegisterClientRequest registerClientRequest, String authorizationSecret) {
        validateAuthorizationSecret(authorizationSecret);

        RegisteredClient registeredClient = AuthenticationClientMapper.map(registerClientRequest, bCryptPasswordEncoder);
        log.debug("{} mapped to {}.", registerClientRequest, registeredClient);
        clientRepository.save(registeredClient);
        log.debug("Client with id {} stored in db.", registeredClient.getClientId());
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public void patchOauth2Client(PatchClientRequest patchClientRequest, String authorizationSecret, String clientId) {
        RegisteredClient oauth2Client = preValidation(authorizationSecret, clientId);

        RegisteredClient newOauth2Client = buildNewOauth2Client(patchClientRequest, oauth2Client);
        log.debug("{} mapped to {}.", patchClientRequest, newOauth2Client);
        clientRepository.save(newOauth2Client);
        log.debug("Client with id {} updated in db.", oauth2Client.getClientId());
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public void deleteOauth2Client(String authorizationSecret, String clientId) {
        preValidation(authorizationSecret, clientId);

        clientRepository.deleteByClientId(clientId);
        log.debug("Client with id {} deleted from db.", clientId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ClientResponse getOauth2Client(String authorizationSecret, String clientId) {
        RegisteredClient oauth2Client = preValidation(authorizationSecret, clientId);

        log.debug("Client with id {} found in db.", clientId);
        return AuthenticationClientMapper.map(oauth2Client);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ClientResponse> getOauth2Clients(String authorizationSecret) {
        validateAuthorizationSecret(authorizationSecret);
        return clientRepository.getAllClients().stream().map(AuthenticationClientMapper::map).toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MetadataResponse getMetadata() {
        MetadataResponse.MetadataResponseBuilder metadataResponseBuilder = MetadataResponse.builder();

        metadataResponseBuilder.issuer(metadataContext.getIssuer());
        metadataResponseBuilder.tokenEndpoint(GENERATE_AUTH_TOKEN_PATH);
        metadataResponseBuilder.jwkSetEndpoint(GET_JWK_SET_PATH);
        metadataResponseBuilder.registerClientEndpoint(API_V1 + "/client");
        metadataResponseBuilder.patchClientEndpoint(API_V1 + CLIENT_BY_ID_PATH);
        metadataResponseBuilder.deleteClientEndpoint(API_V1 + CLIENT_BY_ID_PATH);
        metadataResponseBuilder.getClientEndpoint(API_V1 + CLIENT_BY_ID_PATH);
        metadataResponseBuilder.scopesSupported(Arrays.stream(Scope.values()).map(Scope::name).collect(Collectors.toSet()));
        metadataResponseBuilder.authGrantTypesSupported(Arrays.stream(AuthorizationGrantType.values()).map(AuthorizationGrantType::name).collect(Collectors.toSet()));
        metadataResponseBuilder.authMethodsSupported(Arrays.stream(AuthenticationMethod.values()).map(AuthenticationMethod::name).collect(Collectors.toSet()));
        metadataResponseBuilder.authSignValuesSupported(Set.of("RS256"));

        return metadataResponseBuilder.build();
    }

    /**
     * PreValidation method for solving code duplication
     *
     * @param authorizationSecret authorization secret to confirm identity
     * @param clientId            client id
     * @return {@link RegisteredClient}
     */
    private RegisteredClient preValidation(String authorizationSecret, String clientId) {
        if (StringUtils.isBlank(clientId)) {
            throw new IllegalArgumentException("client_id must not be blank.");
        }

        validateAuthorizationSecret(authorizationSecret);

        RegisteredClient oauth2Client = clientRepository.findByClientId(clientId);
        if (oauth2Client == null) {
            log.warn("Client with id {} not found in db.", clientId);
            throw new EntityNotFoundException(String.format("Client with id %s not found in db.", clientId));
        }

        return oauth2Client;
    }

    /**
     * Method to buld new oauth2 client
     *
     * @param patchClientRequest patch request entity
     * @param oldOauth2Client    old oauth2 client
     * @return {@link RegisteredClient}
     */
    @SuppressWarnings("java:S3776")
    private RegisteredClient buildNewOauth2Client(PatchClientRequest patchClientRequest,
                                                  RegisteredClient oldOauth2Client) {
        RegisteredClient.Builder newOauth2ClientBuilder = RegisteredClient.withId(oldOauth2Client.getId()).clientId(oldOauth2Client.getClientId());

        final String clientSecret = patchClientRequest.clientSecret();
        final String clientName = patchClientRequest.clientName();
        final List<Scope> scopes = patchClientRequest.scopes();
        final List<AuthenticationMethod> clientAuthenticationMethods = patchClientRequest.clientAuthenticationMethods();
        final List<AuthorizationGrantType> clientAuthorizationGrantTypes = patchClientRequest.clientAuthorizationGrantTypes();
        final Integer tokenTimeToLive = patchClientRequest.tokenTimeToLive();

        if (clientSecret != null) {
            if (clientSecret.isBlank()) {
                throw new IllegalArgumentException("client_secret must not be blank if specified.");
            }
            newOauth2ClientBuilder.clientSecret(bCryptPasswordEncoder.encode(clientSecret));
        } else {
            newOauth2ClientBuilder.clientSecret(oldOauth2Client.getClientSecret());
        }

        if (clientName != null) {
            newOauth2ClientBuilder.clientName(clientName);
        } else {
            newOauth2ClientBuilder.clientName(oldOauth2Client.getClientName());
        }

        if (scopes != null) {
            if (!scopes.isEmpty()) {
                for (Scope scope : scopes) {
                    newOauth2ClientBuilder.scope(scope.name());
                }
            }
        } else {
            for (String scope : oldOauth2Client.getScopes()) {
                newOauth2ClientBuilder.scope(scope);
            }
        }

        if (clientAuthenticationMethods != null && !clientAuthenticationMethods.isEmpty()) {
            for (AuthenticationMethod clientAuthenticationMethod : clientAuthenticationMethods) {
                newOauth2ClientBuilder.clientAuthenticationMethod(new ClientAuthenticationMethod(clientAuthenticationMethod.getAuthenticationMethodName()));
            }
        } else {
            for (ClientAuthenticationMethod clientAuthenticationMethod : oldOauth2Client.getClientAuthenticationMethods()) {
                newOauth2ClientBuilder.clientAuthenticationMethod(clientAuthenticationMethod);
            }
        }

        if (clientAuthorizationGrantTypes != null && !clientAuthorizationGrantTypes.isEmpty()) {
            for (AuthorizationGrantType clientAuthorizationGrantType : clientAuthorizationGrantTypes) {
                newOauth2ClientBuilder.authorizationGrantType(new org.springframework.security.oauth2.core.AuthorizationGrantType(clientAuthorizationGrantType.getAuthorizationGrantTypeName()));
            }
        } else {
            for (org.springframework.security.oauth2.core.AuthorizationGrantType clientAuthorizationGrantType : oldOauth2Client.getAuthorizationGrantTypes()) {
                newOauth2ClientBuilder.authorizationGrantType(clientAuthorizationGrantType);
            }
        }

        TokenSettings tokenSettings = oldOauth2Client.getTokenSettings();

        if (tokenTimeToLive != null) {
            newOauth2ClientBuilder.tokenSettings(TokenSettings.withSettings(tokenSettings.getSettings()).accessTokenTimeToLive(Duration.ofMinutes(tokenTimeToLive)).build());
        } else {
            newOauth2ClientBuilder.tokenSettings(TokenSettings.withSettings(tokenSettings.getSettings()).build());
        }

        return newOauth2ClientBuilder.build();
    }

    /**
     * Method to validate authorization secret
     *
     * @param authorizationSecret authorization secret
     */
    private void validateAuthorizationSecret(String authorizationSecret) {
        if (StringUtils.isBlank(authorizationSecret)) {
            throw new InsufficientAuthenticationException("Unauthorized.");
        }

        if (!this.authorizationSecret.equals(authorizationSecret)) {
            throw new InsufficientAuthenticationException("Unauthorized.");
        }
    }
}

