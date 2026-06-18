package com.moving.reservation.review;

import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "고객 리뷰 응답")
public record ReviewApiResponse(
        @Schema(description = "리뷰 번호", example = "1")
        Long id,
        @Schema(description = "리뷰가 연결된 예약 번호", example = "1")
        Long reservationId,
        @Schema(description = "평점", example = "5")
        Integer rating,
        @Schema(description = "리뷰 내용", example = "친절하고 정확했습니다.")
        String content,
        @Schema(description = "리뷰 작성 시각", example = "2026-06-18T16:30:00")
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
