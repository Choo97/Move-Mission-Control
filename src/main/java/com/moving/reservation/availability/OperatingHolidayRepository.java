package com.moving.reservation.availability;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OperatingHolidayRepository extends JpaRepository<OperatingHoliday, Long> {
    Optional<OperatingHoliday> findByHolidayDate(LocalDate holidayDate);
    List<OperatingHoliday> findAllByHolidayDateGreaterThanEqualOrderByHolidayDateAsc(LocalDate date);
}
