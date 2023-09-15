package com.authorizationserver.config;

import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientCredentialsAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.JwtGenerator;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.security.oauth2.server.authorization.web.authentication.OAuth2ClientCredentialsAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.List;
import java.util.Map;

import static com.authorizationserver.constants.Constants.GENERATE_AUTH_TOKEN_PATH;
import static com.authorizationserver.constants.Constants.GET_JWK_SET_PATH;
import static org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration.applyDefaultSecurity;

/**
 * Configuration class for spring authorization server
 *
 * @author Blajan George
 */
@Slf4j
@Configuration
@EnableWebSecurity
@Import(OAuth2AuthorizationServerConfiguration.class)
public class AuthorizationServerConfig {
    /**
     * White listed claims
     */
    @Value("#{'${claims.whiteList}'.split(',')}")
    private List<String> whiteListedClaims;
    /**
     * @param http {@link HttpSecurity} HttpSecurity container class
     * @return {@link SecurityFilterChain}
     * @throws Exception Exception thrown by 'applyDefaultSecurity'
     */
    @Bean
    public SecurityFilterChain authorizeFilterChain(HttpSecurity http) throws Exception {
        OAuth2AuthorizationServerConfigurer authorizationServerConfigurer =
                new OAuth2AuthorizationServerConfigurer();
        http.apply(authorizationServerConfigurer);

        authorizationServerConfigurer
                .tokenEndpoint(tokenEndpoint ->
                        tokenEndpoint.accessTokenRequestConverter(new OAuth2ClientCredentialsAuthenticationConverter())
                );

        applyDefaultSecurity(http);
        return http.build();
    }

    /**
     * Bean to config {@link AuthorizationServerSettings}
     *
     * @return {@link AuthorizationServerSettings}
     */
    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings
                .builder()
                .tokenEndpoint(GENERATE_AUTH_TOKEN_PATH)
                .jwkSetEndpoint(GET_JWK_SET_PATH)
                .build();
    }

    /**
     * Bean to config token generator
     *
     * @return {@link OAuth2TokenGenerator}
     */
    @Bean
    public OAuth2TokenGenerator<Jwt> tokenGenerator(JWKSource<SecurityContext> jwkSource) {
        JwtGenerator jwtGenerator = new JwtGenerator(new NimbusJwtEncoder(jwkSource));
        jwtGenerator.setJwtCustomizer(jwtCustomizer());
        return jwtGenerator;
    }

    /**
     * Bean to config a jwt customizer, we need this to add custom claims to JWT
     *
     * @return {@link OAuth2TokenCustomizer}
     */
    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> jwtCustomizer() {
        return context -> {
            final OAuth2ClientCredentialsAuthenticationToken authenticationToken = context.getAuthorizationGrant();
            final JwtClaimsSet.Builder claims = context.getClaims();
            final Map<String, Object> additionalParams = authenticationToken.getAdditionalParameters();
            for (Map.Entry<String, Object> entry : additionalParams.entrySet()) {
                if(!whiteListedClaims.contains(entry.getKey().toLowerCase())) {
                    log.warn("Claim {} not white listed.", entry.getKey().toLowerCase());
                    throw new IllegalArgumentException(String.format("Claim %s not white listed.", entry.getKey().toLowerCase()));
                }

                claims.claim(entry.getKey().toLowerCase(), entry.getValue());
            }

            claims.build();
        };
    }

    /**
     * Used to salt and hash client secrets
     *
     * @return {@link BCryptPasswordEncoder}
     */
    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

}

