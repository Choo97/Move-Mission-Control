package com.moving.reservation.reservation;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findAllByOrderByMoveDateAscMoveTimeAsc();

    long countByStatus(ReservationStatus status);
}
