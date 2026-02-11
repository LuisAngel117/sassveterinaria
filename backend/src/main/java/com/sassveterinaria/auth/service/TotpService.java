package com.sassveterinaria.auth.service;

import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.time.Instant;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Service;

@Service
public class TotpService {

    private static final String HMAC_ALGORITHM = "HmacSHA1";
    private static final String BASE32_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int SECRET_BYTES = 20;
    private static final int DIGITS = 6;
    private static final long TIME_STEP_SECONDS = 30L;
    private static final int WINDOW_STEPS = 1;

    public String generateSecret() {
        byte[] bytes = new byte[SECRET_BYTES];
        RANDOM.nextBytes(bytes);
        return base32Encode(bytes);
    }

    public String buildOtpAuthUri(String issuer, String accountName, String secret) {
        String encodedIssuer = urlEncode(issuer);
        String encodedAccount = urlEncode(accountName);
        return "otpauth://totp/" + encodedIssuer + ":" + encodedAccount
            + "?secret=" + secret
            + "&issuer=" + encodedIssuer
            + "&algorithm=SHA1&digits=" + DIGITS
            + "&period=" + TIME_STEP_SECONDS;
    }

    public boolean verifyCode(String secret, String code) {
        if (code == null || !code.matches("\\d{6}")) {
            return false;
        }
        long nowCounter = Instant.now().getEpochSecond() / TIME_STEP_SECONDS;
        for (int offset = -WINDOW_STEPS; offset <= WINDOW_STEPS; offset++) {
            String expected = generateCode(secret, nowCounter + offset);
            if (expected.equals(code)) {
                return true;
            }
        }
        return false;
    }

    public String generateCurrentCode(String secret) {
        long counter = Instant.now().getEpochSecond() / TIME_STEP_SECONDS;
        return generateCode(secret, counter);
    }

    private String generateCode(String base32Secret, long counter) {
        try {
            byte[] secret = base32Decode(base32Secret);
            byte[] counterBytes = ByteBuffer.allocate(8).order(ByteOrder.BIG_ENDIAN).putLong(counter).array();

            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(secret, HMAC_ALGORITHM));
            byte[] hmac = mac.doFinal(counterBytes);

            int offset = hmac[hmac.length - 1] & 0x0F;
            int binary = ((hmac[offset] & 0x7F) << 24)
                | ((hmac[offset + 1] & 0xFF) << 16)
                | ((hmac[offset + 2] & 0xFF) << 8)
                | (hmac[offset + 3] & 0xFF);

            int otp = binary % 1_000_000;
            return String.format("%06d", otp);
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("Cannot generate TOTP", ex);
        }
    }

    private String base32Encode(byte[] data) {
        StringBuilder out = new StringBuilder((data.length * 8 + 4) / 5);
        int buffer = 0;
        int bitsLeft = 0;
        for (byte b : data) {
            buffer = (buffer << 8) | (b & 0xFF);
            bitsLeft += 8;
            while (bitsLeft >= 5) {
                int index = (buffer >> (bitsLeft - 5)) & 0x1F;
                bitsLeft -= 5;
                out.append(BASE32_ALPHABET.charAt(index));
            }
        }
        if (bitsLeft > 0) {
            int index = (buffer << (5 - bitsLeft)) & 0x1F;
            out.append(BASE32_ALPHABET.charAt(index));
        }
        return out.toString();
    }

    private byte[] base32Decode(String secret) {
        String normalized = secret.replace("=", "").replace(" ", "").toUpperCase();
        int buffer = 0;
        int bitsLeft = 0;
        byte[] out = new byte[(normalized.length() * 5) / 8];
        int outIndex = 0;

        for (int i = 0; i < normalized.length(); i++) {
            int val = BASE32_ALPHABET.indexOf(normalized.charAt(i));
            if (val < 0) {
                throw new IllegalArgumentException("Invalid Base32 char in secret");
            }
            buffer = (buffer << 5) | val;
            bitsLeft += 5;
            if (bitsLeft >= 8) {
                out[outIndex++] = (byte) ((buffer >> (bitsLeft - 8)) & 0xFF);
                bitsLeft -= 8;
            }
        }
        if (outIndex == out.length) {
            return out;
        }
        byte[] trimmed = new byte[outIndex];
        System.arraycopy(out, 0, trimmed, 0, outIndex);
        return trimmed;
    }

    private String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
