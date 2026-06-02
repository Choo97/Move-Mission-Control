package com.moving.reservation.notification;

public enum NotificationType {

    RESERVATION_CREATED("예약 접수"),
    STATUS_CHANGED("상태 변경"),
    ESTIMATE_UPDATED("견적 안내");

    private final String label;

    NotificationType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
