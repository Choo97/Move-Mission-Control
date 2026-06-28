package com.moving.reservation.admin;

import com.moving.reservation.reservation.ReservationStatus;

public record AdminReservationStatusOptionResponse(
        String status,
        String statusLabel,
        String description,
        String nextAction,
        boolean current
) {

    public static AdminReservationStatusOptionResponse from(ReservationStatus status,
                                                            ReservationStatus currentStatus) {
        return new AdminReservationStatusOptionResponse(
                status.name(),
                status.getLabel(),
                status.getDescription(),
                status.getNextAction(),
                status == currentStatus
        );
    }
}
