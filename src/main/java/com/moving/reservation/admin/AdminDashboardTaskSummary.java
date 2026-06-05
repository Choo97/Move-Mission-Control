package com.moving.reservation.admin;

public record AdminDashboardTaskSummary(
        long receivedCount,
        long consultingCount,
        long estimateAcceptancePendingCount,
        long distancePendingCount,
        long failedEmailCount
) {

    public long totalCount() {
        return receivedCount
                + consultingCount
                + estimateAcceptancePendingCount
                + distancePendingCount
                + failedEmailCount;
    }
}
