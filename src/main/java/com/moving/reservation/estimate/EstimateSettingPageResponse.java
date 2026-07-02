package com.moving.reservation.estimate;

import java.util.List;

public record EstimateSettingPageResponse(
        List<EstimateSettingResponse> settings,
        List<EstimateSettingHistoryResponse> histories
) {
}
