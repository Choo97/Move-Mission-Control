package com.moving.reservation.reservation;

import java.time.LocalDateTime;

public record ReservationApiPhotoResponse(
        Long id,
        String originalFilename,
        String fileUrl,
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
