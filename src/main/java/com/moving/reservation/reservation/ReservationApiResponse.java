package com.moving.reservation.reservation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public record ReservationApiResponse(
        Long id,
        String customerName,
        String email,
        LocalDate moveDate,
        LocalTime moveTime,
        String fromAddress,
        String toAddress,
        String moveType,
        String moveTypeLabel,
        String status,
        String statusLabel,
        boolean fromElevator,
        boolean toElevator,
        Integer fromFloor,
        Integer toFloor,
        boolean fromLadderTruck,
        boolean toLadderTruck,
        Integer distanceKm,
        Integer estimatedPrice,
        Integer baseEstimatedPrice,
        Integer discountAmount,
        Integer finalEstimatedPrice,
        String couponCode,
        String couponName,
        boolean editable,
        boolean cancelable,
        boolean estimateAcceptable,
        boolean estimateAccepted,
        Integer acceptedEstimatePrice,
        LocalDateTime estimateAcceptedAt,
        List<ReservationApiEstimateLineResponse> estimateLines
) {

    public static ReservationApiResponse from(Reservation reservation, List<ReservationEstimateLine> estimateLines) {
        return new ReservationApiResponse(
                reservation.getId(),
                reservation.getCustomerName(),
                reservation.getEmail(),
                reservation.getMoveDate(),
                reservation.getMoveTime(),
                reservation.getFromAddress(),
                reservation.getToAddress(),
                reservation.getMoveType().name(),
                reservation.getMoveType().getLabel(),
                reservation.getStatus().name(),
                reservation.getStatus().getLabel(),
                reservation.isFromElevator(),
                reservation.isToElevator(),
                reservation.getFromFloor(),
                reservation.getToFloor(),
                reservation.isFromLadderTruck(),
                reservation.isToLadderTruck(),
                reservation.getDistanceKm(),
                reservation.getEstimatedPrice(),
                reservation.getBaseEstimatedPrice(),
                reservation.getAppliedDiscountAmount(),
                reservation.getFinalEstimatedPrice(),
                reservation.getCouponCode(),
                reservation.getCouponName(),
                reservation.isEditable(),
                reservation.isCancelable(),
                reservation.isEstimateAcceptable(),
                reservation.hasEstimateAcceptance(),
                reservation.getAcceptedEstimatePrice(),
                reservation.getEstimateAcceptedAt(),
                estimateLines.stream()
                        .map(ReservationApiEstimateLineResponse::from)
                        .toList()
        );
    }
}
