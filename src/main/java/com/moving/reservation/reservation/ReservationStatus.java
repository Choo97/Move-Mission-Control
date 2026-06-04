package com.moving.reservation.reservation;

import java.util.List;

public enum ReservationStatus {
    RECEIVED("접수", "예약이 들어왔고 아직 상담을 시작하기 전입니다.", "고객 연락 후 상담중으로 변경"),
    CONSULTING("상담중", "주소, 날짜, 짐 양, 현장 조건을 확인하는 단계입니다.", "견적을 저장해 견적안내로 변경"),
    ESTIMATE_SENT("견적안내", "고객에게 안내할 견적이 준비된 단계입니다.", "고객 동의 후 확정"),
    CONFIRMED("확정", "고객이 견적에 동의했고 이사 일정이 확정된 단계입니다.", "이사 완료 후 완료 처리"),
    COMPLETED("완료", "이사가 끝난 예약입니다.", "추가 상태 변경 없음"),
    CANCELED("취소", "고객 또는 관리자가 취소한 예약입니다.", "추가 상태 변경 없음");

    private final String label;
    private final String description;
    private final String nextAction;

    ReservationStatus(String label, String description, String nextAction) {
        this.label = label;
        this.description = description;
        this.nextAction = nextAction;
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }

    public String getNextAction() {
        return nextAction;
    }

    public boolean canTransitionTo(ReservationStatus nextStatus) {
        return nextStatuses().contains(nextStatus);
    }

    public List<ReservationStatus> getSelectableStatuses() {
        return switch (this) {
            case RECEIVED -> List.of(RECEIVED, CONSULTING, CANCELED);
            case CONSULTING -> List.of(CONSULTING, ESTIMATE_SENT, CANCELED);
            case ESTIMATE_SENT -> List.of(ESTIMATE_SENT, CONFIRMED, CONSULTING, CANCELED);
            case CONFIRMED -> List.of(CONFIRMED, COMPLETED, CANCELED);
            case COMPLETED -> List.of(COMPLETED);
            case CANCELED -> List.of(CANCELED);
        };
    }

    private List<ReservationStatus> nextStatuses() {
        return switch (this) {
            case RECEIVED -> List.of(CONSULTING, CANCELED);
            case CONSULTING -> List.of(ESTIMATE_SENT, CANCELED);
            case ESTIMATE_SENT -> List.of(CONFIRMED, CONSULTING, CANCELED);
            case CONFIRMED -> List.of(COMPLETED, CANCELED);
            case COMPLETED, CANCELED -> List.of();
        };
    }
}
