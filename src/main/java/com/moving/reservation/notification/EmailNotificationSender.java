package com.moving.reservation.notification;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@EnableConfigurationProperties(EmailNotificationProperties.class)
public class EmailNotificationSender {

    private final JavaMailSender javaMailSender;
    private final EmailNotificationProperties properties;

    public EmailNotificationSender(JavaMailSender javaMailSender, EmailNotificationProperties properties) {
        this.javaMailSender = javaMailSender;
        this.properties = properties;
    }

    public void send(CustomerNotification notification) {
        if (!properties.isEnabled()) {
            throw new IllegalStateException("이메일 발송 설정이 비활성화되어 있습니다.");
        }

        if (!StringUtils.hasText(properties.getFrom())) {
            throw new IllegalStateException("이메일 발신자 주소가 설정되지 않았습니다.");
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(properties.getFrom());
        message.setTo(notification.getRecipientContact());
        message.setSubject(properties.getSubjectPrefix() + " 예약 확인 안내");
        message.setText(notification.getMessage());
        javaMailSender.send(message);
    }
}
