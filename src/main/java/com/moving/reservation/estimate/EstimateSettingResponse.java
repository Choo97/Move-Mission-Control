package com.moving.reservation.estimate;

public record EstimateSettingResponse(
        Long id,
        String settingKey,
        String label,
        int amount,
        String unit
) {

    public static EstimateSettingResponse from(EstimateSetting setting) {
        return new EstimateSettingResponse(
                setting.getId(),
                setting.getSettingKey().name(),
                setting.getLabel(),
                setting.getAmount(),
                unit(setting.getSettingKey())
        );
    }

    static String unit(EstimateSettingKey settingKey) {
        return settingKey == EstimateSettingKey.INCLUDED_DISTANCE_KM ? "km" : "원";
    }
}
