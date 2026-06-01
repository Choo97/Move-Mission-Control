package com.moving.reservation.reservation;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping("/new")
    public String newReservation(Model model) {
        model.addAttribute("reservationCreateRequest", new ReservationCreateRequest());
        model.addAttribute("moveTypes", MoveType.values());
        return "reservation/new";
    }

    @GetMapping("/search")
    public String searchForm(Model model) {
        model.addAttribute("reservationSearchRequest", new ReservationSearchRequest());
        return "reservation/search";
    }

    @PostMapping("/search")
    public String search(@Valid @ModelAttribute ReservationSearchRequest request,
                         BindingResult bindingResult,
                         Model model) {
        if (bindingResult.hasErrors()) {
            return "reservation/search";
        }

        try {
            Reservation reservation = reservationService.search(request);
            return "redirect:/reservations/" + reservation.getId();
        } catch (IllegalArgumentException exception) {
            model.addAttribute("searchError", exception.getMessage());
            return "reservation/search";
        }
    }

    @PostMapping
    public String create(@Valid @ModelAttribute ReservationCreateRequest request,
                         BindingResult bindingResult,
                         Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("moveTypes", MoveType.values());
            return "reservation/new";
        }

        Reservation reservation = reservationService.create(request);
        return "redirect:/reservations/" + reservation.getId();
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("reservation", reservationService.get(id));
        return "reservation/detail";
    }

    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable Long id,
                         @RequestParam String phone,
                         RedirectAttributes redirectAttributes) {
        try {
            reservationService.cancel(id, phone);
            redirectAttributes.addFlashAttribute("cancelMessage", "예약이 취소되었습니다.");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("cancelError", exception.getMessage());
        }

        return "redirect:/reservations/" + id;
    }
}
