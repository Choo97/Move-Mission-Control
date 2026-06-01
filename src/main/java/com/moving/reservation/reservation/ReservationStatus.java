package com.moving.reservation.reservation;

public enum ReservationStatus {
    RECEIVED("접수"),
    CONSULTING("상담중"),
    CONFIRMED("확정"),
    COMPLETED("완료"),
    CANCELED("취소");

    private final String label;

    ReservationStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
