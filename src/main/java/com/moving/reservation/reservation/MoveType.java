package com.moving.reservation.reservation;

public enum MoveType {
    STUDIO("원룸"),
    TWO_ROOM("투룸"),
    FAMILY("가정집"),
    OFFICE("사무실"),
    STORAGE("보관 이사");

    private final String label;

    MoveType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
