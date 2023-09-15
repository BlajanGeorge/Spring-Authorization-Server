package com.authorizationserver.config;

import com.authorizationserver.db.model.RsaKey;
import com.authorizationserver.db.repository.RsaKeyRepository;
import com.authorizationserver.model.RSADto;
import com.authorizationserver.util.CryptographyUtils;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSelector;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import com.authorizationserver.exception.KeyGenerationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Custom JWKSource entity to provide JWKSet based on keys from db
 *
 * @author Blajan George
 */
@Slf4j
@Component
public class CustomJWKSource implements JWKSource<SecurityContext> {
    /**
     * Number of keys retrieved for JWK Set endpoint
     */
    @Value("${jwkSetEndpoint.numberOfRetrievedKeys:5}")
    private Integer numberOfRetrievedPublicKeys;

    /**
     * Encryption secret
     */
    @Value("${encryptionSecret}")
    private String encryptionSecret;

    /**
     * Repository to retrieve keys
     */
    private final RsaKeyRepository rsaKeyRepository;
    /**
     * Key factory
     */
    KeyFactory keyFactory;

    public CustomJWKSource(RsaKeyRepository rsaKeyRepository) throws NoSuchAlgorithmException {
        this.rsaKeyRepository = rsaKeyRepository;
        this.keyFactory = KeyFactory.getInstance("RSA");
    }

    /**
     * Retrieve list of JWKs
     *
     * @param jwkSelector     jwk selector
     * @param securityContext security context
     * @return {@link JWK}
     */
    @Override
    public List<JWK> get(JWKSelector jwkSelector, SecurityContext securityContext) {
        Optional<RsaKey> optionalRsaKey = rsaKeyRepository.getMostRecentKey();

        if (optionalRsaKey.isEmpty()) {
            try {
                return jwkSelector.select(generateNewKeySet());
            } catch (Exception e) {
                log.error("Error encountered when generating a new key set.", e);
                throw new KeyGenerationException(e.getMessage());
            }
        } else {
            try {
                return jwkSelector.select(obtainJWKSetFroDb(optionalRsaKey.get()));
            } catch (Exception e) {
                log.error("Error encountered when obtain the key set from db.", e);
                throw new KeyGenerationException(e.getMessage());
            }
        }
    }

    /**
     * Get last n public keys for jwk set endpoint
     *
     * @param jwkSelector jwk select
     * @return {@link JWK}
     * @throws InvalidKeySpecException invalid key exception
     */
    public List<JWK> getLastNPublicKeys(JWKSelector jwkSelector) throws InvalidKeySpecException {
        List<JWK> rsaKeyList = new ArrayList<>();
        List<RsaKey> keys = rsaKeyRepository.getLimitNMostRecentPublicKeys(numberOfRetrievedPublicKeys);

        for (RsaKey key : keys) {
            X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(key.getPublicKey());
            RSAPublicKey rsaPublicKey = (RSAPublicKey) keyFactory.generatePublic(publicKeySpec);
            rsaKeyList.add(new RSAKey.Builder(rsaPublicKey).keyID(key.getId()).build());
        }

        return jwkSelector.select(new JWKSet(rsaKeyList));
    }

    /**
     * Obtain most recent JWKSet from db
     *
     * @param rsaKey rsa key
     * @return {@link JWKSet}
     * @throws InvalidKeySpecException thrown when a specification for key is invalid
     */
    private JWKSet obtainJWKSetFroDb(RsaKey rsaKey) throws Exception {
        X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(rsaKey.getPublicKey());
        PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(CryptographyUtils.decrypt(rsaKey.getPrivateKey(), encryptionSecret, rsaKey.getIv()));

        RSAPublicKey rsaPublicKey = (RSAPublicKey) keyFactory.generatePublic(publicKeySpec);
        RSAPrivateKey rsaPrivateKey = (RSAPrivateKey) keyFactory.generatePrivate(privateKeySpec);

        return new JWKSet(new RSAKey.Builder(rsaPublicKey).privateKey(rsaPrivateKey).keyID(rsaKey.getId()).build());
    }

    /**
     * Generates new {@link JWKSet} if doesn't exist in db
     *
     * @return {@link JWKSet}
     * @throws NoSuchAlgorithmException thrown when specified alg doesn't exist
     */
    private JWKSet generateNewKeySet() throws Exception {
        RSADto rsaDto = CryptographyUtils.generateNewRsaKey();
        byte[] iv = CryptographyUtils.generateIv();

        rsaKeyRepository.saveAndFlush(new RsaKey(rsaDto.rsaKey().getKeyID(), rsaDto.rsaPublicKey().getEncoded(), CryptographyUtils.encrypt(rsaDto.rsaPrivateKey().getEncoded(), encryptionSecret, iv), iv, Instant.now()));

        return new JWKSet(rsaDto.rsaKey());
    }

}
