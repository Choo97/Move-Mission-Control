package com.moving.reservation.reservation;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findAllByOrderByMoveDateAscMoveTimeAsc();

    @Query("""
            select reservation
            from Reservation reservation
            where (:status is null or reservation.status = :status)
              and (
                :keyword is null
                or lower(reservation.customerName) like concat('%', :keyword, '%')
                or reservation.phone like concat('%', :keyword, '%')
              )
            order by reservation.moveDate asc, reservation.moveTime asc
            """)
    List<Reservation> search(@Param("status") ReservationStatus status, @Param("keyword") String keyword);

    Optional<Reservation> findByIdAndPhone(Long id, String phone);

    long countByStatus(ReservationStatus status);
}
