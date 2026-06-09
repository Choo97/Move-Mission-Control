package com.moving.reservation.reservation;

public enum CustomerActionType {
    UPDATE("예약 수정"),
    CANCEL("예약 취소");

    private final String label;

    CustomerActionType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
