package com.moving.reservation.reservation;

public record ReservationSummary(
        long total,
        long received,
        long consulting,
        long estimateSent,
        long confirmed,
        long completed,
        long today,
        long couponUsages,
        double averageRating
) {
}
