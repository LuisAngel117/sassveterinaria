package com.sassveterinaria.auth.service;

import com.sassveterinaria.security.JwtProperties;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Service;

@Service
public class TotpCryptoService {

    private static final String AES = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int IV_SIZE = 12;
    private static final int TAG_LENGTH_BITS = 128;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final SecretKeySpec key;

    public TotpCryptoService(JwtProperties jwtProperties) {
        this.key = new SecretKeySpec(deriveKey(jwtProperties.secret()), AES);
    }

    public String encrypt(String plaintext) {
        try {
            byte[] iv = new byte[IV_SIZE];
            RANDOM.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_LENGTH_BITS, iv));
            byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            return "v1:" + Base64.getEncoder().encodeToString(iv) + ":" + Base64.getEncoder().encodeToString(ciphertext);
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("Cannot encrypt TOTP secret", ex);
        }
    }

    public String decrypt(String encodedCiphertext) {
        if (encodedCiphertext == null || encodedCiphertext.isBlank()) {
            return null;
        }
        try {
            String[] parts = encodedCiphertext.split(":");
            if (parts.length != 3 || !"v1".equals(parts[0])) {
                throw new IllegalArgumentException("Invalid encrypted format");
            }
            byte[] iv = Base64.getDecoder().decode(parts[1]);
            byte[] ciphertext = Base64.getDecoder().decode(parts[2]);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_LENGTH_BITS, iv));
            byte[] plaintext = cipher.doFinal(ciphertext);
            return new String(plaintext, StandardCharsets.UTF_8);
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("Cannot decrypt TOTP secret", ex);
        }
    }

    private byte[] deriveKey(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest((input + ":totp-v1").getBytes(StandardCharsets.UTF_8));
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("Cannot derive encryption key", ex);
        }
    }
}
