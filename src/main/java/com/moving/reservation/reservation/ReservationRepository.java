package com.moving.reservation.reservation;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findAllByOrderByMoveDateAscMoveTimeAsc();

    List<Reservation> findTop5ByOrderByCreatedAtDesc();

    @Query("""
            select reservation
            from Reservation reservation
            where (:status is null or reservation.status = :status)
              and (
                :keyword is null
                or lower(reservation.customerName) like concat('%', :keyword, '%')
                or reservation.phone like concat('%', :keyword, '%')
              )
              and (:startDate is null or reservation.moveDate >= :startDate)
              and (:endDate is null or reservation.moveDate <= :endDate)
              and (:needsDistance is null or :needsDistance = false or reservation.distanceKm is null)
            order by reservation.moveDate asc, reservation.moveTime asc
            """)
    List<Reservation> search(@Param("status") ReservationStatus status,
                             @Param("keyword") String keyword,
                             @Param("startDate") LocalDate startDate,
                             @Param("endDate") LocalDate endDate,
                             @Param("needsDistance") Boolean needsDistance);

    Optional<Reservation> findByIdAndPhone(Long id, String phone);

    boolean existsByMoveDateAndMoveTimeAndStatusNot(LocalDate moveDate,
                                                     java.time.LocalTime moveTime,
                                                     ReservationStatus status);

    @Query("""
            select reservation
            from Reservation reservation
            where reservation.couponCode is not null
              and reservation.couponCode <> ''
            order by reservation.createdAt desc
            """)
    List<Reservation> findCouponUsages();

    long countByStatus(ReservationStatus status);

    long countByMoveDate(LocalDate moveDate);

    long countByStatusAndEstimateAcceptedAtIsNull(ReservationStatus status);

    long countByStatusInAndDistanceKmIsNull(Collection<ReservationStatus> statuses);

    @Query("""
            select count(reservation)
            from Reservation reservation
            where reservation.couponCode is not null
              and reservation.couponCode <> ''
            """)
    long countCouponUsages();
}
