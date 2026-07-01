package com.moving.reservation.admin;

import com.moving.reservation.reservation.ReservationConflictAttempt;
import com.moving.reservation.privacy.PersonalInfoMasker;
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
                PersonalInfoMasker.maskName(attempt.getCustomerName()),
                PersonalInfoMasker.maskPhone(attempt.getPhone()),
                attempt.getMoveDate(),
                attempt.getMoveTime(),
                attempt.getAttemptedAt()
        );
    }
}
