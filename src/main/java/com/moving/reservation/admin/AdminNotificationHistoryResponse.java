package com.moving.reservation.admin;

import com.moving.reservation.notification.CustomerNotification;
import java.time.LocalDateTime;

public record AdminNotificationHistoryResponse(
        Long id,
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

    public static AdminNotificationHistoryResponse from(CustomerNotification notification) {
        return new AdminNotificationHistoryResponse(
                notification.getId(),
                notification.getType().name(),
                notification.getType().getLabel(),
                notification.getChannel().name(),
                notification.getChannel().getLabel(),
                notification.getStatus().name(),
                notification.getStatus().getLabel(),
                notification.getRecipientContact(),
                notification.getMessage(),
                notification.getFailureReason(),
                notification.getSentAt(),
                notification.getCreatedAt()
        );
    }
}
