package com.moving.reservation.privacy;

import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PrivacyHashService {

    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final SecretKeySpec secretKey;

    public PrivacyHashService(@Value("${privacy.hash.secret:move-mission-local-privacy-hash-secret}") String secret) {
        this.secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM);
    }

    public String phoneHash(String phone) {
        if (phone == null || phone.isBlank()) {
            return null;
        }

        return hmac(normalizePhone(phone));
    }

    public boolean matchesPhone(String rawPhone, String candidatePhone) {
        String left = normalizePhone(rawPhone);
        String right = normalizePhone(candidatePhone);

        return !left.isBlank() && left.equals(right);
    }

    private String normalizePhone(String phone) {
        return phone == null ? "" : phone.replaceAll("\\D", "");
    }

    private String hmac(String value) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(secretKey);

            return HexFormat.of().formatHex(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("민감정보 비교 해시를 생성하는 중 문제가 발생했습니다.", exception);
        }
    }
}
