package com.moving.reservation.reservation;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findAllByOrderByMoveDateAscMoveTimeAsc();

    Optional<Reservation> findByIdAndPhone(Long id, String phone);

    long countByStatus(ReservationStatus status);
}
