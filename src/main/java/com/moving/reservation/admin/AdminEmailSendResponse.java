package com.moving.reservation.admin;

public record AdminEmailSendResponse(
        int sentCount,
        int failedCount,
        AdminReservationDetailResponse reservation
) {
}
