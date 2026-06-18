package com.moving.reservation.reservation;

import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "짐 사진 업로드 응답")
public record ReservationApiPhotoResponse(
        @Schema(description = "사진 번호", example = "1")
        Long id,
        @Schema(description = "고객이 업로드한 원본 파일명", example = "boxes.jpg")
        String originalFilename,
        @Schema(description = "브라우저에서 접근할 수 있는 사진 URL", example = "/uploads/reservations/1/boxes.jpg")
        String fileUrl,
        @Schema(description = "사진 업로드 시각", example = "2026-06-18T16:30:00")
        LocalDateTime uploadedAt
) {

    public static ReservationApiPhotoResponse from(ReservationPhoto photo) {
        return new ReservationApiPhotoResponse(
                photo.getId(),
                photo.getOriginalFilename(),
                photo.getFileUrl(),
                photo.getUploadedAt()
        );
    }
}
