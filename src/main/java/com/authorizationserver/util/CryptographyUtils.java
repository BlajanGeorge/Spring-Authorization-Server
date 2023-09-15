package com.authorizationserver.util;

import com.nimbusds.jose.jwk.RSAKey;
import com.authorizationserver.db.model.RsaKey;
import com.authorizationserver.model.RSADto;
import lombok.experimental.UtilityClass;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.UUID;

/**
 * Utility class for Rsa keys
 *
 * @author Blajan George
 */
@UtilityClass
public class CryptographyUtils {
    /**
     * Algorithm and mode used for encryption
     */
    private static final String AES_GCM = "AES/GCM/NoPadding";
    /**
     * Algorithm used for encryption
     */
    private static final String AES = "AES";

    /**
     * Generates new {@link RsaKey}
     *
     * @return {@link RSADto}
     * @throws NoSuchAlgorithmException thrown when specified alg doesn't exist
     */
    public static RSADto generateNewRsaKey() throws NoSuchAlgorithmException {
        KeyPair keyPair = generateRsaKey();

        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();

        RSAKey rsaKey = new RSAKey.Builder(publicKey).privateKey(privateKey).keyID(UUID.randomUUID().toString()).build();

        return new RSADto(rsaKey, privateKey, publicKey);
    }

    /**
     * Generates a {@link KeyPair}
     *
     * @return {@link KeyPair}
     * @throws NoSuchAlgorithmException thrown when specified alg doesn't exist
     */
    private KeyPair generateRsaKey() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        return keyPairGenerator.generateKeyPair();
    }

    /**
     * Method to encrypt private rsa keys stored in db
     *
     * @param data private rsa key
     * @return {@link Byte} encrypted rsa key
     */
    public static byte[] encrypt(byte[] data, String encryptionSecret, byte[] iv) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
        Key key = new SecretKeySpec(encryptionSecret.getBytes(), AES);
        Cipher c = Cipher.getInstance(AES_GCM);
        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(128, iv);
        c.init(Cipher.ENCRYPT_MODE, key, gcmParameterSpec);
        return c.doFinal(data);
    }

    /**
     * Method to decrypt private rsa keys stored in db
     *
     * @param encryptedData private rsa key encrypted
     * @return {@link Byte} decrypted rsa key
     */
    public static byte[] decrypt(byte[] encryptedData, String encryptionSecret, byte[] iv) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
        Key key = new SecretKeySpec(encryptionSecret.getBytes(), AES);
        Cipher c = Cipher.getInstance(AES_GCM);
        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(128, iv);
        c.init(Cipher.DECRYPT_MODE, key, gcmParameterSpec);

        return c.doFinal(encryptedData);
    }

    /**
     * Generate an initialization vector for GCM
     *
     * @return {@link Byte}
     */
    public static byte[] generateIv() {
        byte[] iv = new byte[12];
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);

        return iv;
    }

}
