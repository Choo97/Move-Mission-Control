package com.moving.reservation.notification;

public record EmailNotificationSendResult(int sentCount, int failedCount) {
}
