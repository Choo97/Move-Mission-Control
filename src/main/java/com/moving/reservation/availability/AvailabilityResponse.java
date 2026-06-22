package com.moving.reservation.availability;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record AvailabilityResponse(
        LocalDate date,
        boolean closed,
        String closureReason,
        List<LocalTime> availableTimes
) {
}
