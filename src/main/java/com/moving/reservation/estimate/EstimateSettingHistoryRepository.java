package com.moving.reservation.estimate;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EstimateSettingHistoryRepository extends JpaRepository<EstimateSettingHistory, Long> {

    List<EstimateSettingHistory> findAllByOrderByChangedAtDesc();
}
