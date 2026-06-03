package com.moving.reservation.estimate;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EstimateSettingRepository extends JpaRepository<EstimateSetting, Long> {

    Optional<EstimateSetting> findBySettingKey(EstimateSettingKey settingKey);

    List<EstimateSetting> findAllByOrderBySortOrderAsc();
}
