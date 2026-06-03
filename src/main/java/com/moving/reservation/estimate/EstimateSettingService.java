package com.moving.reservation.estimate;

import java.util.Arrays;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class EstimateSettingService {

    private final EstimateSettingRepository estimateSettingRepository;

    public EstimateSettingService(EstimateSettingRepository estimateSettingRepository) {
        this.estimateSettingRepository = estimateSettingRepository;
    }

    public List<EstimateSetting> findAll() {
        return estimateSettingRepository.findAllByOrderBySortOrderAsc();
    }

    public int amount(EstimateSettingKey settingKey) {
        return estimateSettingRepository.findBySettingKey(settingKey)
                .map(EstimateSetting::getAmount)
                .orElse(settingKey.getDefaultAmount());
    }

    @Transactional
    public void update(Long id, int amount) {
        EstimateSetting estimateSetting = estimateSettingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("견적 기준을 찾을 수 없습니다."));
        estimateSetting.updateAmount(amount);
    }

    @Transactional
    public void initializeDefaults() {
        Arrays.stream(EstimateSettingKey.values())
                .filter(settingKey -> estimateSettingRepository.findBySettingKey(settingKey).isEmpty())
                .map(EstimateSetting::new)
                .forEach(estimateSettingRepository::save);
    }
}
