package com.moving.reservation.reservation;

public class ReservationScheduleConflictException extends RuntimeException {

    public ReservationScheduleConflictException() {
        super("선택한 날짜와 시간에는 이미 예약이 있습니다. 다른 시간을 선택해 주세요.");
    }

    public ReservationScheduleConflictException(String message) {
        super(message);
    }
}
