package com.vect.vect.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MfaServiceTest {

    private MfaService mfaService;

    @BeforeEach
    void setUp() {
        mfaService = new MfaService(Base64.getEncoder().encodeToString(new byte[32]));
        mfaService.validateEncryptionKey();
    }

    @Test
    void encryptsAndDecryptsProvisioningSecret() {
        String secret = mfaService.generateSecret();

        assertThat(mfaService.decrypt(mfaService.encrypt(secret))).isEqualTo(secret);
    }

    @Test
    void validatesCurrentTotpCodeAndRejectsMalformedCodes() {
        String secret = "GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ";
        Instant time = Instant.ofEpochSecond(59);

        assertThat(mfaService.matchingStep(secret, "287082", time)).hasValue(1);
        assertThat(mfaService.matchingStep(secret, "12345", time)).isEmpty();
        assertThat(mfaService.matchingStep(secret, "abcdef", time)).isEmpty();
    }

    @Test
    void requiresExactlyA32ByteEncryptionKey() {
        MfaService invalid = new MfaService("not-base64");

        assertThatThrownBy(invalid::validateEncryptionKey)
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Base64-encoded 32-byte key");
    }
}
