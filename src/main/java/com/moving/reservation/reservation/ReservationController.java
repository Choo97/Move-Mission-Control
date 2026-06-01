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
}
