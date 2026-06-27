package com.moving.reservation.reservation;

public enum CustomerRequestType {

    UPDATE("예약 수정 요청"),
    CANCEL("예약 취소 요청");

    private final String label;

    CustomerRequestType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
