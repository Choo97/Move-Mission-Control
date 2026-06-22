package com.moving.reservation.config;

import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "privacy.retention")
public record PrivacyRetentionProperties(
        @Min(1) int conflictAttemptDays
) {

    public PrivacyRetentionProperties {
        if (conflictAttemptDays == 0) {
            conflictAttemptDays = 30;
        }
    }
}
