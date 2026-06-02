package com.moving.reservation.notification;

public enum NotificationStatus {

    READY("발송 준비"),
    SENT("발송 완료"),
    FAILED("발송 실패");

    private final String label;

    NotificationStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
