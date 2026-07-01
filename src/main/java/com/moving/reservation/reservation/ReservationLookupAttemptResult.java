package com.moving.reservation.reservation;

public record ReservationLookupAttemptResult(
        boolean allowed,
        String message,
        long retryAfterSeconds
) {

    public static ReservationLookupAttemptResult allow() {
        return new ReservationLookupAttemptResult(true, null, 0);
    }

    public static ReservationLookupAttemptResult block(String message, long retryAfterSeconds) {
        return new ReservationLookupAttemptResult(false, message, retryAfterSeconds);
    }
}
