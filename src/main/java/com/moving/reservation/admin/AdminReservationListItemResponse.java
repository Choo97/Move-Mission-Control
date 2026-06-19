package com.moving.reservation.admin;

import com.moving.reservation.reservation.Reservation;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record AdminReservationListItemResponse(
        Long id,
        String customerName,
        String phone,
        LocalDate moveDate,
        LocalTime moveTime,
        String fromAddress,
        String toAddress,
        String moveType,
        String moveTypeLabel,
        String status,
        String statusLabel,
        Integer distanceKm,
        Integer finalEstimatedPrice,
        LocalDateTime createdAt
) {

    public static AdminReservationListItemResponse from(Reservation reservation) {
        return new AdminReservationListItemResponse(
                reservation.getId(),
                reservation.getCustomerName(),
                reservation.getPhone(),
                reservation.getMoveDate(),
                reservation.getMoveTime(),
                reservation.getFromAddress(),
                reservation.getToAddress(),
                reservation.getMoveType().name(),
                reservation.getMoveType().getLabel(),
                reservation.getStatus().name(),
                reservation.getStatus().getLabel(),
                reservation.getDistanceKm(),
                reservation.getFinalEstimatedPrice(),
                reservation.getCreatedAt()
        );
    }
}
