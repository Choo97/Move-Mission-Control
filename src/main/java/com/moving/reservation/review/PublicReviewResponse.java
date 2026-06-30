package com.moving.reservation.review;

import com.moving.reservation.reservation.Reservation;
import java.time.LocalDateTime;
import org.springframework.util.StringUtils;

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
                maskedCustomerName(reservation.getCustomerName()),
                review.getRating(),
                review.getContent(),
                review.getAdminReply(),
                reservation.getMoveType().getLabel(),
                review.getCreatedAt()
        );
    }

    private static String maskedCustomerName(String customerName) {
        if (!StringUtils.hasText(customerName)) {
            return "이사 고객";
        }

        String trimmedName = customerName.trim();
        return trimmedName.substring(0, 1) + "** 고객";
    }
}
