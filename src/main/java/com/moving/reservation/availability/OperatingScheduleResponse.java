package com.moving.reservation.availability;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record OperatingScheduleResponse(
        Long id,
        DayOfWeek dayOfWeek,
        String dayLabel,
        boolean open,
        LocalTime startTime,
        LocalTime endTime,
        int slotMinutes
) {
    public static OperatingScheduleResponse from(OperatingSchedule schedule) {
        return new OperatingScheduleResponse(
                schedule.getId(), schedule.getDayOfWeek(), dayLabel(schedule.getDayOfWeek()),
                schedule.isOpen(), schedule.getStartTime(), schedule.getEndTime(), schedule.getSlotMinutes()
        );
    }

    private static String dayLabel(DayOfWeek day) {
        return switch (day) {
            case MONDAY -> "월요일";
            case TUESDAY -> "화요일";
            case WEDNESDAY -> "수요일";
            case THURSDAY -> "목요일";
            case FRIDAY -> "금요일";
            case SATURDAY -> "토요일";
            case SUNDAY -> "일요일";
        };
    }
}
