package com.moving.reservation.admin;

import com.moving.reservation.notification.CustomerNotification;
import com.moving.reservation.privacy.PersonalInfoMasker;
import com.moving.reservation.reservation.Reservation;
import java.time.LocalDateTime;

public record AdminNotificationListItemResponse(
        Long id,
        Long reservationId,
        String customerName,
        String phone,
        String email,
        String type,
        String typeLabel,
        String channel,
        String channelLabel,
        String status,
        String statusLabel,
        String recipientContact,
        String message,
        String failureReason,
        LocalDateTime sentAt,
        LocalDateTime createdAt
) {

    public static AdminNotificationListItemResponse from(CustomerNotification notification) {
        Reservation reservation = notification.getReservation();

        return new AdminNotificationListItemResponse(
                notification.getId(),
                reservation.getId(),
                PersonalInfoMasker.maskName(reservation.getCustomerName()),
                PersonalInfoMasker.maskPhone(reservation.getPhone()),
                PersonalInfoMasker.maskEmail(reservation.getEmail()),
                notification.getType().name(),
                notification.getType().getLabel(),
                notification.getChannel().name(),
                notification.getChannel().getLabel(),
                notification.getStatus().name(),
                notification.getStatus().getLabel(),
                PersonalInfoMasker.maskContact(notification.getRecipientContact()),
                notification.getMessage(),
                notification.getFailureReason(),
                notification.getSentAt(),
                notification.getCreatedAt()
        );
    }
}
