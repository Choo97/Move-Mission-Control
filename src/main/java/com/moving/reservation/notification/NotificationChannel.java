package com.moving.reservation.notification;

public enum NotificationChannel {

    SMS("SMS"),
    KAKAO_ALIMTALK("카카오 알림톡");

    private final String label;

    NotificationChannel(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
