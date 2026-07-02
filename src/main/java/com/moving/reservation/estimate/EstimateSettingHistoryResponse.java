package com.moving.reservation.estimate;

import java.time.LocalDateTime;

public record EstimateSettingHistoryResponse(
        Long id,
        String settingKey,
        String label,
        int previousAmount,
        int changedAmount,
        String unit,
        String changedBy,
        LocalDateTime changedAt
) {

    public static EstimateSettingHistoryResponse from(EstimateSettingHistory history) {
        return new EstimateSettingHistoryResponse(
                history.getId(),
                history.getSettingKey().name(),
                history.getLabel(),
                history.getPreviousAmount(),
                history.getChangedAmount(),
                EstimateSettingResponse.unit(history.getSettingKey()),
                history.getChangedBy(),
                history.getChangedAt()
        );
    }
}
