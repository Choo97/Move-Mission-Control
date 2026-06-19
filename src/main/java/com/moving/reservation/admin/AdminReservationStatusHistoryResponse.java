package com.moving.reservation.admin;

import com.moving.reservation.reservation.ReservationStatusHistory;
import java.time.LocalDateTime;

public record AdminReservationStatusHistoryResponse(
        Long id,
        String previousStatus,
        String previousStatusLabel,
        String changedStatus,
        String changedStatusLabel,
        LocalDateTime changedAt,
        String changedBy
) {

    public static AdminReservationStatusHistoryResponse from(ReservationStatusHistory history) {
        return new AdminReservationStatusHistoryResponse(
                history.getId(),
                history.getPreviousStatus().name(),
                history.getPreviousStatus().getLabel(),
                history.getChangedStatus().name(),
                history.getChangedStatus().getLabel(),
                history.getChangedAt(),
                history.getChangedBy()
        );
    }
}
