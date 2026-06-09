package com.moving.reservation.reservation;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationCustomerActionHistoryRepository extends JpaRepository<ReservationCustomerActionHistory, Long> {

    List<ReservationCustomerActionHistory> findByReservationIdOrderByCreatedAtDesc(Long reservationId);
}
