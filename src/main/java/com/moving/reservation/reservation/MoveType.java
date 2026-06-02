package com.moving.reservation.reservation;

public enum MoveType {
    STUDIO("원룸", 180000),
    TWO_ROOM("투룸", 280000),
    FAMILY("가정집", 450000),
    OFFICE("사무실", 550000),
    STORAGE("보관 이사", 350000);

    private final String label;
    private final int basePrice;

    MoveType(String label, int basePrice) {
        this.label = label;
        this.basePrice = basePrice;
    }

    public String getLabel() {
        return label;
    }

    public int getBasePrice() {
        return basePrice;
    }
}
