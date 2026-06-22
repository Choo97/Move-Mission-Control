package com.moving.reservation.availability;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;

public record OperatingScheduleUpdateRequest(
        boolean open,
        @NotNull LocalTime startTime,
        @NotNull LocalTime endTime,
        @Min(30) @Max(240) int slotMinutes
) {
}
