package com.moving.reservation.admin;

public record AdminSmsSendResponse(
        int sentCount,
        int failedCount,
        AdminReservationDetailResponse reservation
) {
}
