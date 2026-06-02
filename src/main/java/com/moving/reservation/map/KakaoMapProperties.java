package com.moving.reservation.map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "map.kakao")
public class KakaoMapProperties {

    private boolean enabled;
    private String restApiKey;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getRestApiKey() {
        return restApiKey;
    }

    public void setRestApiKey(String restApiKey) {
        this.restApiKey = restApiKey;
    }

    public boolean isReady() {
        return enabled && restApiKey != null && !restApiKey.isBlank();
    }
}
