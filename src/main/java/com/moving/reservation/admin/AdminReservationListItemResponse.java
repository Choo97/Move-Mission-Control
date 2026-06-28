package com.moving.reservation.admin;

import com.moving.reservation.reservation.Reservation;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record AdminReservationListItemResponse(
        Long id,
        String customerName,
        String phone,
        LocalDate moveDate,
        LocalTime moveTime,
        String fromAddress,
        String toAddress,
        String moveType,
        String moveTypeLabel,
        String status,
        String statusLabel,
        String nextActionLabel,
        String nextActionDescription,
        boolean attentionRequired,
        Integer distanceKm,
        Integer finalEstimatedPrice,
        LocalDateTime createdAt
) {

    public static AdminReservationListItemResponse from(Reservation reservation) {
        return new AdminReservationListItemResponse(
                reservation.getId(),
                reservation.getCustomerName(),
                reservation.getPhone(),
                reservation.getMoveDate(),
                reservation.getMoveTime(),
                reservation.getFromAddress(),
                reservation.getToAddress(),
                reservation.getMoveType().name(),
                reservation.getMoveType().getLabel(),
                reservation.getStatus().name(),
                reservation.getStatus().getLabel(),
                nextActionLabel(reservation),
                nextActionDescription(reservation),
                attentionRequired(reservation),
                reservation.getDistanceKm(),
                reservation.getFinalEstimatedPrice(),
                reservation.getCreatedAt()
        );
    }

    private static String nextActionLabel(Reservation reservation) {
        if (!attentionRequired(reservation)) {
            return reservation.getStatus().getNextAction();
        }

        if (reservation.getDistanceKm() == null) {
            return "거리 확인";
        }

        return switch (reservation.getStatus()) {
            case RECEIVED -> "상담 시작";
            case CONSULTING -> "견적 안내";
            case ESTIMATE_SENT -> "동의 확인";
            case CONFIRMED -> "완료 처리";
            case COMPLETED, CANCELED -> reservation.getStatus().getNextAction();
        };
    }

    private static String nextActionDescription(Reservation reservation) {
        if (!attentionRequired(reservation)) {
            return reservation.getStatus().getDescription();
        }

        if (reservation.getDistanceKm() == null) {
            return "거리 입력 후 견적을 점검하세요.";
        }

        return reservation.getStatus().getNextAction();
    }

    private static boolean attentionRequired(Reservation reservation) {
        return switch (reservation.getStatus()) {
            case COMPLETED, CANCELED -> false;
            case RECEIVED, CONSULTING, ESTIMATE_SENT, CONFIRMED -> true;
        };
    }
}
