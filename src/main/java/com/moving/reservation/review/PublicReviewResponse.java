package com.moving.reservation.review;

import com.moving.reservation.reservation.Reservation;
import com.moving.reservation.privacy.PersonalInfoMasker;
import java.time.LocalDateTime;

public record PublicReviewResponse(
        Long id,
        String customerName,
        Integer rating,
        String content,
        String adminReply,
        String moveTypeLabel,
        LocalDateTime createdAt
) {

    public static PublicReviewResponse from(Review review) {
        Reservation reservation = review.getReservation();
        return new PublicReviewResponse(
                review.getId(),
                PersonalInfoMasker.maskReviewCustomerName(reservation.getCustomerName()),
                review.getRating(),
                review.getContent(),
                review.getAdminReply(),
                reservation.getMoveType().getLabel(),
                review.getCreatedAt()
        );
    }
}
