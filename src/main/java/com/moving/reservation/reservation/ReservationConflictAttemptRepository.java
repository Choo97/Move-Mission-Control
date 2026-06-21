package com.moving.reservation.reservation;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationConflictAttemptRepository extends JpaRepository<ReservationConflictAttempt, Long> {

    List<ReservationConflictAttempt> findTop10ByOrderByAttemptedAtDesc();
}
