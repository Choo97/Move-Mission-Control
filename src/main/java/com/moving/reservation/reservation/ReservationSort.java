package com.moving.reservation.reservation;

public enum ReservationSort {
    PRIORITY("처리 우선순위"),
    MOVE_DATE("이사일 빠른순"),
    CREATED_DESC("최근 접수순");

    private final String label;

    ReservationSort(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
