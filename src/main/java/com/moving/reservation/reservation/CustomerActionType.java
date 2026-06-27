package com.moving.reservation.reservation;

public enum CustomerActionType {
    UPDATE("예약 수정"),
    CANCEL("예약 취소"),
    UPDATE_REQUEST("예약 수정 요청"),
    CANCEL_REQUEST("예약 취소 요청"),
    REQUEST_APPROVED("요청 승인"),
    REQUEST_REJECTED("요청 반려");

    private final String label;

    CustomerActionType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
