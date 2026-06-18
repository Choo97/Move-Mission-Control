package com.moving.reservation.review;

import java.time.LocalDateTime;

public record ReviewApiResponse(
        Long id,
        Long reservationId,
        Integer rating,
        String content,
        LocalDateTime createdAt
) {

    public static ReviewApiResponse from(Review review) {
        return new ReviewApiResponse(
                review.getId(),
                review.getReservation().getId(),
                review.getRating(),
                review.getContent(),
                review.getCreatedAt()
        );
    }
}
