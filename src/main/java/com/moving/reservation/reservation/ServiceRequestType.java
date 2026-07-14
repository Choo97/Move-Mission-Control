package com.moving.reservation.reservation;

public enum ServiceRequestType {
    GENERAL("일반 이사 예약"),
    NON_PROFIT("비영리 도움 요청");

    private final String label;

    ServiceRequestType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public boolean isNonProfit() {
        return this == NON_PROFIT;
    }
}
