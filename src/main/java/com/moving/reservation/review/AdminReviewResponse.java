package com.moving.reservation.review;

import com.moving.reservation.reservation.Reservation;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record AdminReviewResponse(
        Long id,
        Long reservationId,
        String customerName,
        String phone,
        String email,
        Integer rating,
        String content,
        LocalDate moveDate,
        LocalTime moveTime,
        String status,
        String statusLabel,
        LocalDateTime createdAt
) {

    public static AdminReviewResponse from(Review review) {
        Reservation reservation = review.getReservation();

        return new AdminReviewResponse(
                review.getId(),
                reservation.getId(),
                reservation.getCustomerName(),
                reservation.getPhone(),
                reservation.getEmail(),
                review.getRating(),
                review.getContent(),
                reservation.getMoveDate(),
                reservation.getMoveTime(),
                reservation.getStatus().name(),
                reservation.getStatus().getLabel(),
                review.getCreatedAt()
        );
    }
}
