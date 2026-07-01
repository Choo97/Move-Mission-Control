package com.moving.reservation.privacy;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PrivacyCryptoService {

    private static final String ENCRYPTED_PREFIX = "enc:v1:";
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int IV_LENGTH_BYTES = 12;
    private static final int AUTH_TAG_LENGTH_BITS = 128;

    private final SecureRandom secureRandom = new SecureRandom();
    private final SecretKeySpec secretKey;

    public PrivacyCryptoService(@Value("${privacy.crypto.secret:move-mission-local-privacy-secret}") String secret) {
        this.secretKey = new SecretKeySpec(sha256(secret), ALGORITHM);
    }

    public String encrypt(String plainText) {
        if (plainText == null) {
            return null;
        }

        if (isEncrypted(plainText)) {
            return plainText;
        }

        byte[] iv = new byte[IV_LENGTH_BYTES];
        secureRandom.nextBytes(iv);

        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(AUTH_TAG_LENGTH_BITS, iv));
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            byte[] payload = ByteBuffer.allocate(iv.length + encrypted.length)
                    .put(iv)
                    .put(encrypted)
                    .array();

            return ENCRYPTED_PREFIX + Base64.getEncoder().encodeToString(payload);
        } catch (Exception exception) {
            throw new IllegalStateException("민감정보를 암호화하는 중 문제가 발생했습니다.", exception);
        }
    }

    public String decrypt(String databaseValue) {
        if (databaseValue == null) {
            return null;
        }

        if (!isEncrypted(databaseValue)) {
            return databaseValue;
        }

        try {
            byte[] payload = Base64.getDecoder().decode(databaseValue.substring(ENCRYPTED_PREFIX.length()));
            byte[] iv = Arrays.copyOfRange(payload, 0, IV_LENGTH_BYTES);
            byte[] encrypted = Arrays.copyOfRange(payload, IV_LENGTH_BYTES, payload.length);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(AUTH_TAG_LENGTH_BITS, iv));

            return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
        } catch (Exception exception) {
            throw new IllegalStateException("민감정보를 복호화하는 중 문제가 발생했습니다.", exception);
        }
    }

    public boolean isEncrypted(String value) {
        return value != null && value.startsWith(ENCRYPTED_PREFIX);
    }

    private byte[] sha256(String value) {
        try {
            return MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
        } catch (Exception exception) {
            throw new IllegalStateException("민감정보 암호화 키를 준비하는 중 문제가 발생했습니다.", exception);
        }
    }
}
