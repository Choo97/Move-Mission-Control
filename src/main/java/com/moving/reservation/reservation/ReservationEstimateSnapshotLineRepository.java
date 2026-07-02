package com.moving.reservation.reservation;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationEstimateSnapshotLineRepository
        extends JpaRepository<ReservationEstimateSnapshotLine, Long> {

    List<ReservationEstimateSnapshotLine> findByReservationIdOrderByLineOrderAsc(Long reservationId);

    void deleteByReservationId(Long reservationId);
}
