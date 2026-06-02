package com.moving.reservation.reservation;

import com.moving.reservation.review.ReviewService;
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
    private final ReviewService reviewService;

    public ReservationController(ReservationService reservationService, ReviewService reviewService) {
        this.reservationService = reservationService;
        this.reviewService = reviewService;
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

        try {
            Reservation reservation = reservationService.create(request);
            return "redirect:/reservations/" + reservation.getId();
        } catch (IllegalArgumentException | IllegalStateException exception) {
            model.addAttribute("moveTypes", MoveType.values());
            model.addAttribute("uploadError", exception.getMessage());
            return "reservation/new";
        }
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("reservation", reservationService.get(id));
        model.addAttribute("photos", reservationService.findPhotos(id));
        model.addAttribute("review", reviewService.findByReservationId(id).orElse(null));
        return "reservation/detail";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Reservation reservation = reservationService.get(id);
        model.addAttribute("reservation", reservation);
        model.addAttribute("reservationUpdateRequest", ReservationUpdateRequest.from(reservation));
        return "reservation/edit";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute ReservationUpdateRequest request,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        Reservation reservation = reservationService.get(id);

        if (bindingResult.hasErrors()) {
            model.addAttribute("reservation", reservation);
            return "reservation/edit";
        }

        try {
            reservationService.updateDetails(id, request);
            redirectAttributes.addFlashAttribute("updateMessage", "예약 정보가 수정되었습니다.");
            return "redirect:/reservations/" + id;
        } catch (IllegalArgumentException exception) {
            model.addAttribute("reservation", reservation);
            model.addAttribute("updateError", exception.getMessage());
            return "reservation/edit";
        }
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
