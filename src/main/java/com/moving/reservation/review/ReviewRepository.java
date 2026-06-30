package com.moving.reservation.review;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    Optional<Review> findByReservationId(Long reservationId);

    boolean existsByReservationId(Long reservationId);

    @Query("""
            select review
            from Review review
            join fetch review.reservation
            where review.id = ?1
            """)
    Optional<Review> findByIdWithReservation(Long id);

    @Query("""
            select review
            from Review review
            join fetch review.reservation
            order by review.createdAt desc, review.id desc
            """)
    List<Review> findAllWithReservationOrderByCreatedAtDesc();

    @Query("""
            select coalesce(avg(review.rating), 0)
            from Review review
            """)
    double averageRating();
}
