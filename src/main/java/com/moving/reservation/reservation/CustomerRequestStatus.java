package com.moving.reservation.reservation;

public enum CustomerRequestStatus {

    PENDING("처리 대기"),
    APPROVED("승인"),
    REJECTED("반려");

    private final String label;

    CustomerRequestStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
