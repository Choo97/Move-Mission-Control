package com.moving.reservation.estimate;

import java.util.Arrays;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class EstimateSettingService {

    private final EstimateSettingRepository estimateSettingRepository;
    private final EstimateSettingHistoryRepository estimateSettingHistoryRepository;

    public EstimateSettingService(EstimateSettingRepository estimateSettingRepository,
                                  EstimateSettingHistoryRepository estimateSettingHistoryRepository) {
        this.estimateSettingRepository = estimateSettingRepository;
        this.estimateSettingHistoryRepository = estimateSettingHistoryRepository;
    }

    public List<EstimateSetting> findAll() {
        return estimateSettingRepository.findAllByOrderBySortOrderAsc();
    }

    public List<EstimateSettingHistory> findHistories() {
        return estimateSettingHistoryRepository.findAllByOrderByChangedAtDesc();
    }

    public int amount(EstimateSettingKey settingKey) {
        return estimateSettingRepository.findBySettingKey(settingKey)
                .map(EstimateSetting::getAmount)
                .orElse(settingKey.getDefaultAmount());
    }

    @Transactional
    public EstimateSetting update(Long id, int amount, String changedBy) {
        EstimateSetting estimateSetting = estimateSettingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("견적 기준을 찾을 수 없습니다."));
        validateAmount(estimateSetting.getSettingKey(), amount);
        int previousAmount = estimateSetting.getAmount();
        if (previousAmount == amount) {
            return estimateSetting;
        }

        estimateSetting.updateAmount(amount);
        estimateSettingHistoryRepository.save(new EstimateSettingHistory(
                estimateSetting,
                previousAmount,
                amount,
                changedBy
        ));
        return estimateSetting;
    }

    @Transactional
    public void initializeDefaults() {
        Arrays.stream(EstimateSettingKey.values())
                .filter(settingKey -> estimateSettingRepository.findBySettingKey(settingKey).isEmpty())
                .map(EstimateSetting::new)
                .forEach(estimateSettingRepository::save);
    }

    private void validateAmount(EstimateSettingKey settingKey, int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("견적 기준 값은 0 이상이어야 합니다.");
        }

        if (settingKey == EstimateSettingKey.INCLUDED_DISTANCE_KM && amount > 500) {
            throw new IllegalArgumentException("기본 포함 이동 거리는 500km 이하로 입력해 주세요.");
        }

        if (settingKey != EstimateSettingKey.INCLUDED_DISTANCE_KM && amount > 10_000_000) {
            throw new IllegalArgumentException("견적 기준 금액은 10,000,000원 이하로 입력해 주세요.");
        }
    }
}
