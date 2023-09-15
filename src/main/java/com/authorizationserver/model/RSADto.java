package com.authorizationserver.model;

import com.nimbusds.jose.jwk.RSAKey;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

/**
 * RSA DTO
 *
 * @author Blajan George
 */
public record RSADto(RSAKey rsaKey, RSAPrivateKey rsaPrivateKey, RSAPublicKey rsaPublicKey) {
}
