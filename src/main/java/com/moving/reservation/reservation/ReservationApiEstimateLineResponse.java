package com.moving.reservation.reservation;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "견적 산정 항목")
public record ReservationApiEstimateLineResponse(
        @Schema(description = "견적 항목명", example = "기본 요금")
        String label,
        @Schema(description = "견적 항목 금액", example = "150000")
        int amount
) {

    public static ReservationApiEstimateLineResponse from(ReservationEstimateLine line) {
        return new ReservationApiEstimateLineResponse(line.label(), line.amount());
    }
}
