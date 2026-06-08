package com.moving.reservation.admin;

import com.moving.reservation.reservation.Reservation;
import java.time.LocalDate;
import java.util.List;

public record AdminCalendarDay(
        LocalDate date,
        boolean currentMonth,
        boolean today,
        boolean selected,
        List<Reservation> reservations
) {
}
