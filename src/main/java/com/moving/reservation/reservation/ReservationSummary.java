package com.moving.reservation.reservation;

public record ReservationSummary(
        long total,
        long received,
        long consulting,
        long confirmed
) {
}
