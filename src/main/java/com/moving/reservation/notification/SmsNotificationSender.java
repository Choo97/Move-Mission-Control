package com.moving.reservation.notification;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@EnableConfigurationProperties(SmsNotificationProperties.class)
public class SmsNotificationSender {

    private static final String MOCK_PROVIDER = "mock";

    private final SmsNotificationProperties properties;

    public SmsNotificationSender(SmsNotificationProperties properties) {
        this.properties = properties;
    }

    public void send(CustomerNotification notification) {
        if (!properties.isEnabled()) {
            throw new IllegalStateException("SMS 발송 설정이 비활성화되어 있습니다.");
        }

        if (!StringUtils.hasText(properties.getFrom())) {
            throw new IllegalStateException("SMS 발신번호가 설정되지 않았습니다.");
        }

        if (!StringUtils.hasText(notification.getRecipientContact())) {
            throw new IllegalArgumentException("SMS 수신번호가 없습니다.");
        }

        if (MOCK_PROVIDER.equalsIgnoreCase(properties.getProvider())) {
            return;
        }

        throw new IllegalStateException("SMS 발송 업체 연동이 아직 연결되지 않았습니다.");
    }
}
