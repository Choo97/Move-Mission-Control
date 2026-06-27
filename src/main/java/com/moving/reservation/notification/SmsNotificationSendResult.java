package com.moving.reservation.notification;

public record SmsNotificationSendResult(int sentCount, int failedCount) {
}
