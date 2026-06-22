package com.moving.reservation.availability;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OperatingScheduleRepository extends JpaRepository<OperatingSchedule, Long> {
    Optional<OperatingSchedule> findByDayOfWeek(DayOfWeek dayOfWeek);
    List<OperatingSchedule> findAllByOrderByDayOfWeekAsc();
}
