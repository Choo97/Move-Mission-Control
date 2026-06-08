package com.moving.reservation.reservation;

public record CustomerReservationStep(
        ReservationStatus status,
        String label,
        String description,
        boolean done,
        boolean current
) {
}
