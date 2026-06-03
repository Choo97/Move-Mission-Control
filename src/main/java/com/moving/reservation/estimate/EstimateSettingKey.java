package com.moving.reservation.estimate;

public enum EstimateSettingKey {

    STUDIO_BASE("원룸 기본가", 180000, 10),
    TWO_ROOM_BASE("투룸 기본가", 280000, 20),
    FAMILY_BASE("가정집 기본가", 450000, 30),
    OFFICE_BASE("사무실 기본가", 550000, 40),
    STORAGE_BASE("보관 이사 기본가", 350000, 50),
    NO_ELEVATOR_SURCHARGE("엘리베이터 없음 추가요금", 50000, 60),
    FLOOR_SURCHARGE("고층 작업 층당 추가요금", 20000, 70),
    LADDER_TRUCK_SURCHARGE("사다리차 추가요금", 120000, 80),
    INCLUDED_DISTANCE_KM("기본 포함 이동 거리(km)", 10, 90),
    DISTANCE_SURCHARGE_PER_KM("거리 초과 km당 추가요금", 10000, 100);

    private final String label;
    private final int defaultAmount;
    private final int sortOrder;

    EstimateSettingKey(String label, int defaultAmount, int sortOrder) {
        this.label = label;
        this.defaultAmount = defaultAmount;
        this.sortOrder = sortOrder;
    }

    public String getLabel() {
        return label;
    }

    public int getDefaultAmount() {
        return defaultAmount;
    }

    public int getSortOrder() {
        return sortOrder;
    }
}
