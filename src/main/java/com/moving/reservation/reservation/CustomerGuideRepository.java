package com.moving.reservation.reservation;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerGuideRepository extends JpaRepository<CustomerGuide, Long> {

    List<CustomerGuide> findAllByOrderByStatusAscDisplayOrderAscIdAsc();

    List<CustomerGuide> findByStatusAndActiveTrueOrderByDisplayOrderAscIdAsc(ReservationStatus status);
}
