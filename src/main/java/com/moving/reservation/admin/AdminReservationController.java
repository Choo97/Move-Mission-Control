package com.moving.reservation.admin;

import com.moving.reservation.notification.CustomerNotificationService;
import com.moving.reservation.reservation.Reservation;
import com.moving.reservation.reservation.ReservationService;
import com.moving.reservation.reservation.ReservationStatus;
import java.security.Principal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/reservations")
public class AdminReservationController {

    private final ReservationService reservationService;
    private final CustomerNotificationService customerNotificationService;

    public AdminReservationController(ReservationService reservationService,
                                      CustomerNotificationService customerNotificationService) {
        this.reservationService = reservationService;
        this.customerNotificationService = customerNotificationService;
    }

    @GetMapping
    public String list(@RequestParam(required = false) ReservationStatus status,
                       @RequestParam(required = false) String keyword,
                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                       Model model) {
        LocalDate currentDate = LocalDate.now();

        model.addAttribute("reservations", reservationService.search(status, keyword, startDate, endDate));
        model.addAttribute("summary", reservationService.summary());
        model.addAttribute("statuses", ReservationStatus.values());
        model.addAttribute("selectedStatus", status);
        model.addAttribute("keyword", keyword);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("today", currentDate);
        model.addAttribute("weekStart", currentDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)));
        model.addAttribute("weekEnd", currentDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)));
        model.addAttribute("monthStart", currentDate.withDayOfMonth(1));
        model.addAttribute("monthEnd", currentDate.with(TemporalAdjusters.lastDayOfMonth()));
        return "admin/reservations";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Reservation reservation = reservationService.get(id);
        model.addAttribute("reservation", reservation);
        model.addAttribute("estimateLines", reservationService.estimateLines(reservation));
        model.addAttribute("photos", reservationService.findPhotos(id));
        model.addAttribute("statusHistories", reservationService.findStatusHistories(id));
        model.addAttribute("notifications", customerNotificationService.findByReservationId(id));
        model.addAttribute("statuses", ReservationStatus.values());
        return "admin/reservation-detail";
    }

    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable Long id, @RequestParam ReservationStatus status, Principal principal) {
        reservationService.updateStatus(id, status, principal.getName());
        return "redirect:/admin/reservations/" + id;
    }

    @PostMapping("/{id}/estimate")
    public String updateEstimate(@PathVariable Long id, @RequestParam Integer estimatedPrice) {
        reservationService.updateEstimate(id, estimatedPrice);
        return "redirect:/admin/reservations/" + id;
    }

    @PostMapping("/{id}/distance")
    public String updateDistance(@PathVariable Long id, @RequestParam(required = false) Integer distanceKm) {
        reservationService.updateDistance(id, distanceKm);
        return "redirect:/admin/reservations/" + id;
    }

    @PostMapping("/{id}/distance/calculate")
    public String calculateDistance(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            int distanceKm = reservationService.calculateAndUpdateDistance(id);
            redirectAttributes.addFlashAttribute("distanceMessage", distanceKm + "km 이동 거리를 자동 계산해 견적에 반영했습니다.");
        } catch (IllegalArgumentException | IllegalStateException exception) {
            redirectAttributes.addFlashAttribute("distanceError", exception.getMessage());
        }

        return "redirect:/admin/reservations/" + id;
    }

    @PostMapping("/{id}/memo")
    public String updateAdminMemo(@PathVariable Long id,
                                  @RequestParam(required = false) String adminMemo,
                                  Principal principal) {
        reservationService.updateAdminMemo(id, adminMemo, principal.getName());
        return "redirect:/admin/reservations/" + id;
    }
}
