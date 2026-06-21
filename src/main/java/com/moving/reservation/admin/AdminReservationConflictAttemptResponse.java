package com.moving.reservation.admin;

import com.moving.reservation.reservation.ReservationConflictAttempt;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record AdminReservationConflictAttemptResponse(
        Long id,
        String customerName,
        String phone,
        LocalDate moveDate,
        LocalTime moveTime,
        LocalDateTime attemptedAt
) {

    public static AdminReservationConflictAttemptResponse from(ReservationConflictAttempt attempt) {
        return new AdminReservationConflictAttemptResponse(
                attempt.getId(),
                attempt.getCustomerName(),
                attempt.getPhone(),
                attempt.getMoveDate(),
                attempt.getMoveTime(),
                attempt.getAttemptedAt()
        );
    }
}
