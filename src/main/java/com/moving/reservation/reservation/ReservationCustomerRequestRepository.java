package com.moving.reservation.reservation;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationCustomerRequestRepository extends JpaRepository<ReservationCustomerRequest, Long> {

    List<ReservationCustomerRequest> findByReservationIdOrderByRequestedAtDesc(Long reservationId);

    boolean existsByReservationIdAndStatus(Long reservationId, CustomerRequestStatus status);

    long countByStatus(CustomerRequestStatus status);
}
