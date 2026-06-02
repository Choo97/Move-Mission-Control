package com.moving.reservation.coupon;

public enum DiscountType {
    FIXED("정액 할인"),
    PERCENT("정률 할인");

    private final String label;

    DiscountType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
