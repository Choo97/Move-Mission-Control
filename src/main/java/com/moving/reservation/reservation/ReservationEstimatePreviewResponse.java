package com.moving.reservation.reservation;

import java.util.List;

public record ReservationEstimatePreviewResponse(
        List<ReservationEstimatePreviewLine> lines,
        int estimatedPrice,
        int discountAmount,
        int finalEstimatedPrice,
        String couponName
) {
}
