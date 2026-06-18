package com.moving.reservation.reservation;

public record ReservationApiEstimateLineResponse(String label, int amount) {

    public static ReservationApiEstimateLineResponse from(ReservationEstimateLine line) {
        return new ReservationApiEstimateLineResponse(line.label(), line.amount());
    }
}
