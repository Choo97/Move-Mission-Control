package com.moving.reservation.availability;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record OperatingHolidayCreateRequest(
        @NotNull @FutureOrPresent LocalDate holidayDate,
        @Size(max = 100) String reason
) {
}
