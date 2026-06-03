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
    public void update(Long id, int amount, String changedBy) {
        EstimateSetting estimateSetting = estimateSettingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("견적 기준을 찾을 수 없습니다."));
        int previousAmount = estimateSetting.getAmount();
        if (previousAmount == amount) {
            return;
        }

        estimateSetting.updateAmount(amount);
        estimateSettingHistoryRepository.save(new EstimateSettingHistory(
                estimateSetting,
                previousAmount,
                amount,
                changedBy
        ));
    }

    @Transactional
    public void initializeDefaults() {
        Arrays.stream(EstimateSettingKey.values())
                .filter(settingKey -> estimateSettingRepository.findBySettingKey(settingKey).isEmpty())
                .map(EstimateSetting::new)
                .forEach(estimateSettingRepository::save);
    }
}
