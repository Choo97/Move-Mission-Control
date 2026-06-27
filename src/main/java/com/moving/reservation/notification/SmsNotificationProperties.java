package com.moving.reservation.notification;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

@ConfigurationProperties(prefix = "notification.sms")
public class SmsNotificationProperties {

    private boolean enabled;
    private String provider = "disabled";
    private String from;
    private String adminTo;
    private String accessKey;
    private String secretKey;
    private String serviceId;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getAdminTo() {
        return adminTo;
    }

    public void setAdminTo(String adminTo) {
        this.adminTo = adminTo;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public boolean hasAdminRecipient() {
        return StringUtils.hasText(adminTo);
    }
}
