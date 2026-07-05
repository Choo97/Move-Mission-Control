package com.moving.reservation.availability;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record OperatingPolicyRequest(
        @Min(0) @Max(30) int minAdvanceDays,
        @Min(1) @Max(365) int maxAdvanceDays,
        @Min(1) @Max(50) int maxDailyReservations,
        OperatingServiceMode serviceMode
) {
}
