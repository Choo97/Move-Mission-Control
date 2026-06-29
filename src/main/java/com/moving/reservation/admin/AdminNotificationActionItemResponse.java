package com.moving.reservation.admin;

import com.moving.reservation.notification.CustomerNotification;
import com.moving.reservation.reservation.Reservation;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record AdminNotificationActionItemResponse(
        Long id,
        Long reservationId,
        String customerName,
        String phone,
        LocalDate moveDate,
        LocalTime moveTime,
        String type,
        String typeLabel,
        String channel,
        String channelLabel,
        String status,
        String statusLabel,
        String recipientContact,
        String message,
        String failureReason,
        LocalDateTime createdAt
) {

    public static AdminNotificationActionItemResponse from(CustomerNotification notification) {
        Reservation reservation = notification.getReservation();

        return new AdminNotificationActionItemResponse(
                notification.getId(),
                reservation.getId(),
                reservation.getCustomerName(),
                reservation.getPhone(),
                reservation.getMoveDate(),
                reservation.getMoveTime(),
                notification.getType().name(),
                notification.getType().getLabel(),
                notification.getChannel().name(),
                notification.getChannel().getLabel(),
                notification.getStatus().name(),
                notification.getStatus().getLabel(),
                notification.getRecipientContact(),
                notification.getMessage(),
                notification.getFailureReason(),
                notification.getCreatedAt()
        );
    }
}
