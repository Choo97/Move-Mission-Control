package com.moving.reservation.admin;

import com.moving.reservation.reservation.Reservation;
import com.moving.reservation.reservation.ReservationService;
import com.moving.reservation.reservation.ReservationSort;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin/calendar")
public class AdminCalendarController {

    private final ReservationService reservationService;

    public AdminCalendarController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping
    public String calendar(@RequestParam(required = false) String month,
                           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                           Model model) {
        LocalDate today = LocalDate.now();
        YearMonth selectedMonth = parseMonth(month, today);
        LocalDate selectedDate = selectedDate(date, selectedMonth, today);
        YearMonth previousMonth = selectedMonth.minusMonths(1);
        YearMonth nextMonth = selectedMonth.plusMonths(1);

        List<Reservation> monthReservations = reservationService.search(
                null,
                null,
                selectedMonth.atDay(1),
                selectedMonth.atEndOfMonth(),
                false,
                ReservationSort.MOVE_DATE
        );
        Map<LocalDate, List<Reservation>> reservationsByDate = monthReservations.stream()
                .collect(Collectors.groupingBy(Reservation::getMoveDate));
        List<List<AdminCalendarDay>> weeks = calendarWeeks(selectedMonth, selectedDate, today, reservationsByDate);
        List<Reservation> selectedReservations = reservationsByDate.getOrDefault(selectedDate, List.of());
        List<Reservation> todayReservations = reservationService.search(
                null,
                null,
                today,
                today,
                false,
                ReservationSort.MOVE_DATE
        );

        model.addAttribute("selectedMonth", selectedMonth);
        model.addAttribute("previousMonth", previousMonth);
        model.addAttribute("nextMonth", nextMonth);
        model.addAttribute("selectedDate", selectedDate);
        model.addAttribute("today", today);
        model.addAttribute("weeks", weeks);
        model.addAttribute("selectedReservations", selectedReservations);
        model.addAttribute("todayReservations", todayReservations);
        model.addAttribute("monthReservationCount", monthReservations.size());
        model.addAttribute("calendarReturnUrl", calendarReturnUrl(selectedMonth, selectedDate));

        return "admin/calendar";
    }

    private String calendarReturnUrl(YearMonth selectedMonth, LocalDate selectedDate) {
        return "/admin/calendar?month=" + selectedMonth + "&date=" + selectedDate;
    }

    private YearMonth parseMonth(String month, LocalDate today) {
        if (month == null || month.isBlank()) {
            return YearMonth.from(today);
        }

        try {
            return YearMonth.parse(month);
        } catch (DateTimeParseException exception) {
            return YearMonth.from(today);
        }
    }

    private LocalDate selectedDate(LocalDate date, YearMonth selectedMonth, LocalDate today) {
        if (date != null && YearMonth.from(date).equals(selectedMonth)) {
            return date;
        }

        if (YearMonth.from(today).equals(selectedMonth)) {
            return today;
        }

        return selectedMonth.atDay(1);
    }

    private List<List<AdminCalendarDay>> calendarWeeks(YearMonth selectedMonth,
                                                       LocalDate selectedDate,
                                                       LocalDate today,
                                                       Map<LocalDate, List<Reservation>> reservationsByDate) {
        LocalDate firstDay = selectedMonth.atDay(1);
        LocalDate lastDay = selectedMonth.atEndOfMonth();
        LocalDate cursor = firstDay.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
        LocalDate calendarEnd = lastDay.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY));
        List<List<AdminCalendarDay>> weeks = new ArrayList<>();

        while (!cursor.isAfter(calendarEnd)) {
            List<AdminCalendarDay> week = new ArrayList<>();

            for (int dayIndex = 0; dayIndex < 7; dayIndex++) {
                boolean currentMonth = YearMonth.from(cursor).equals(selectedMonth);
                week.add(new AdminCalendarDay(
                        cursor,
                        currentMonth,
                        cursor.equals(today),
                        cursor.equals(selectedDate),
                        currentMonth ? reservationsByDate.getOrDefault(cursor, List.of()) : List.of()
                ));
                cursor = cursor.plusDays(1);
            }

            weeks.add(week);
        }

        return weeks;
    }
}
