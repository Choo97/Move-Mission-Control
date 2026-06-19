package com.moving.reservation.admin;

import com.moving.reservation.reservation.ReservationCustomerActionHistory;
import java.time.LocalDateTime;

public record AdminReservationCustomerActionHistoryResponse(
        Long id,
        String actionType,
        String summary,
        String detail,
        String requestedBy,
        LocalDateTime createdAt
) {

    public static AdminReservationCustomerActionHistoryResponse from(ReservationCustomerActionHistory history) {
        return new AdminReservationCustomerActionHistoryResponse(
                history.getId(),
                history.getActionType().name(),
                history.getSummary(),
                history.getDetail(),
                history.getRequestedBy(),
                history.getCreatedAt()
        );
    }
}
