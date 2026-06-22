package com.moving.reservation.availability;

import java.time.LocalDate;

public record OperatingHolidayResponse(Long id, LocalDate holidayDate, String reason) {
    public static OperatingHolidayResponse from(OperatingHoliday holiday) {
        return new OperatingHolidayResponse(holiday.getId(), holiday.getHolidayDate(), holiday.getReason());
    }
}
