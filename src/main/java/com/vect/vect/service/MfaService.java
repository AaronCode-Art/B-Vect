package com.vect.vect.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Arrays;
import java.util.Base64;
import java.util.OptionalLong;

@Service
public class MfaService {

    private static final String ENCRYPTION_ALGORITHM = "AES/GCM/NoPadding";
    private static final String TOTP_ALGORITHM = "HmacSHA1";
    private static final int SECRET_BYTES = 20;
    private static final int CODE_DIGITS = 6;
    private static final long STEP_SECONDS = 30;
    private static final long ALLOWED_DRIFT_STEPS = 1;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final String encryptionKey;

    public MfaService(@Value("${app.mfa.encryption-key:}") String encryptionKey) {
        this.encryptionKey = encryptionKey;
    }

    @PostConstruct
    void validateEncryptionKey() {
        try {
            if (Base64.getDecoder().decode(encryptionKey).length != 32) {
                throw new IllegalStateException("MFA_ENCRYPTION_KEY must decode to exactly 32 bytes");
            }
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException("MFA_ENCRYPTION_KEY must be a Base64-encoded 32-byte key", exception);
        }
    }

    public String generateSecret() {
        byte[] secret = new byte[SECRET_BYTES];
        SECURE_RANDOM.nextBytes(secret);
        return encodeBase32(secret);
    }

    public String provisioningUri(String secret, String email, String issuer) {
        String label = urlEncode(issuer) + ":" + urlEncode(email);
        return "otpauth://totp/" + label
            + "?secret=" + secret
            + "&issuer=" + urlEncode(issuer)
            + "&algorithm=SHA1&digits=6&period=30";
    }

    public String encrypt(String secret) {
        try {
            byte[] nonce = new byte[12];
            SECURE_RANDOM.nextBytes(nonce);
            Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, encryptionKey(), new GCMParameterSpec(128, nonce));
            byte[] encrypted = cipher.doFinal(secret.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(ByteBuffer.allocate(nonce.length + encrypted.length)
                .put(nonce).put(encrypted).array());
        } catch (Exception exception) {
            throw new IllegalStateException("No se pudo cifrar el secreto MFA", exception);
        }
    }

    public String decrypt(String encryptedSecret) {
        try {
            byte[] encrypted = Base64.getDecoder().decode(encryptedSecret);
            if (encrypted.length <= 12) {
                throw new IllegalArgumentException("Invalid encrypted MFA secret");
            }
            byte[] nonce = Arrays.copyOfRange(encrypted, 0, 12);
            Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, encryptionKey(), new GCMParameterSpec(128, nonce));
            return new String(cipher.doFinal(encrypted, nonce.length, encrypted.length - nonce.length),
                StandardCharsets.UTF_8);
        } catch (Exception exception) {
            throw new IllegalStateException("No se pudo descifrar el secreto MFA; verifica MFA_ENCRYPTION_KEY",
                exception);
        }
    }

    public OptionalLong matchingStep(String secret, String code, Instant now) {
        if (code == null || !code.matches("\\d{6}")) {
            return OptionalLong.empty();
        }
        long currentStep = now.getEpochSecond() / STEP_SECONDS;
        byte[] suppliedCode = code.getBytes(StandardCharsets.US_ASCII);
        for (long step = currentStep - ALLOWED_DRIFT_STEPS; step <= currentStep + ALLOWED_DRIFT_STEPS; step++) {
            if (MessageDigest.isEqual(suppliedCode, totp(secret, step).getBytes(StandardCharsets.US_ASCII))) {
                return OptionalLong.of(step);
            }
        }
        return OptionalLong.empty();
    }

    private SecretKeySpec encryptionKey() {
        return new SecretKeySpec(Base64.getDecoder().decode(encryptionKey), "AES");
    }

    private String totp(String secret, long step) {
        try {
            Mac mac = Mac.getInstance(TOTP_ALGORITHM);
            mac.init(new SecretKeySpec(decodeBase32(secret), TOTP_ALGORITHM));
            byte[] hash = mac.doFinal(ByteBuffer.allocate(Long.BYTES).putLong(step).array());
            int offset = hash[hash.length - 1] & 0x0f;
            int binary = ((hash[offset] & 0x7f) << 24)
                | ((hash[offset + 1] & 0xff) << 16)
                | ((hash[offset + 2] & 0xff) << 8)
                | (hash[offset + 3] & 0xff);
            return String.format("%06d", binary % 1_000_000);
        } catch (Exception exception) {
            throw new IllegalStateException("No se pudo validar el código MFA", exception);
        }
    }

    private String encodeBase32(byte[] bytes) {
        final char[] alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567".toCharArray();
        StringBuilder encoded = new StringBuilder((bytes.length * 8 + 4) / 5);
        int buffer = 0;
        int bitsLeft = 0;
        for (byte value : bytes) {
            buffer = (buffer << 8) | (value & 0xff);
            bitsLeft += 8;
            while (bitsLeft >= 5) {
                encoded.append(alphabet[(buffer >> (bitsLeft - 5)) & 0x1f]);
                bitsLeft -= 5;
            }
        }
        if (bitsLeft > 0) {
            encoded.append(alphabet[(buffer << (5 - bitsLeft)) & 0x1f]);
        }
        return encoded.toString();
    }

    private byte[] decodeBase32(String value) {
        String normalized = value.replace("=", "").toUpperCase(java.util.Locale.ROOT);
        ByteBuffer output = ByteBuffer.allocate(normalized.length() * 5 / 8);
        int buffer = 0;
        int bitsLeft = 0;
        for (char character : normalized.toCharArray()) {
            int index = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567".indexOf(character);
            if (index < 0) {
                throw new IllegalArgumentException("Invalid Base32 MFA secret");
            }
            buffer = (buffer << 5) | index;
            bitsLeft += 5;
            if (bitsLeft >= 8) {
                output.put((byte) (buffer >> (bitsLeft - 8)));
                bitsLeft -= 8;
            }
        }
        return Arrays.copyOf(output.array(), output.position());
    }

    private String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }
}
