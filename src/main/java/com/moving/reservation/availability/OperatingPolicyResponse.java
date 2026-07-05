package com.moving.reservation.availability;

public record OperatingPolicyResponse(
        int minAdvanceDays,
        int maxAdvanceDays,
        int maxDailyReservations,
        OperatingServiceMode serviceMode
) {

    public static OperatingPolicyResponse from(OperatingPolicy policy) {
        return new OperatingPolicyResponse(
                policy.getMinAdvanceDays(),
                policy.getMaxAdvanceDays(),
                policy.getMaxDailyReservations(),
                policy.getServiceMode()
        );
    }
}
