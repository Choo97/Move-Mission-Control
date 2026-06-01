package com.moving.reservation.admin;

import com.moving.reservation.reservation.ReservationService;
import com.moving.reservation.reservation.ReservationStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin/reservations")
public class AdminReservationController {

    private final ReservationService reservationService;

    public AdminReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("reservations", reservationService.findAll());
        model.addAttribute("summary", reservationService.summary());
        model.addAttribute("statuses", ReservationStatus.values());
        return "admin/reservations";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("reservation", reservationService.get(id));
        model.addAttribute("statusHistories", reservationService.findStatusHistories(id));
        model.addAttribute("statuses", ReservationStatus.values());
        return "admin/reservation-detail";
    }

    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable Long id, @RequestParam ReservationStatus status) {
        reservationService.updateStatus(id, status);
        return "redirect:/admin/reservations/" + id;
    }

    @PostMapping("/{id}/estimate")
    public String updateEstimate(@PathVariable Long id, @RequestParam Integer estimatedPrice) {
        reservationService.updateEstimate(id, estimatedPrice);
        return "redirect:/admin/reservations/" + id;
    }

    @PostMapping("/{id}/memo")
    public String updateAdminMemo(@PathVariable Long id, @RequestParam(required = false) String adminMemo) {
        reservationService.updateAdminMemo(id, adminMemo);
        return "redirect:/admin/reservations/" + id;
    }
}
