package com.moving.reservation.reservation;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationPhotoRepository extends JpaRepository<ReservationPhoto, Long> {

    List<ReservationPhoto> findByReservationIdOrderByUploadedAtAsc(Long reservationId);

    long countByReservationId(Long reservationId);
}
