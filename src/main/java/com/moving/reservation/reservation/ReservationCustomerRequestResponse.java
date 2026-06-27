package com.moving.reservation.reservation;

import java.time.LocalDateTime;

public record ReservationCustomerRequestResponse(
        Long id,
        String requestType,
        String requestTypeLabel,
        String status,
        String statusLabel,
        String detail,
        String rejectionReason,
        String requestedBy,
        LocalDateTime requestedAt,
        String processedBy,
        LocalDateTime processedAt
) {

    public static ReservationCustomerRequestResponse from(ReservationCustomerRequest request) {
        return new ReservationCustomerRequestResponse(
                request.getId(),
                request.getRequestType().name(),
                request.getRequestType().getLabel(),
                request.getStatus().name(),
                request.getStatus().getLabel(),
                request.getDetail(),
                request.getRejectionReason(),
                request.getRequestedBy(),
                request.getRequestedAt(),
                request.getProcessedBy(),
                request.getProcessedAt()
        );
    }
}
