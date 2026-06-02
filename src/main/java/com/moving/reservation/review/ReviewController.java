package com.moving.reservation.review;

import com.moving.reservation.reservation.Reservation;
import com.moving.reservation.reservation.ReservationService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewService reviewService;
    private final ReservationService reservationService;

    public ReviewController(ReviewService reviewService, ReservationService reservationService) {
        this.reviewService = reviewService;
        this.reservationService = reservationService;
    }

    @GetMapping("/new")
    public String newReview(@RequestParam Long reservationId, Model model) {
        Reservation reservation = reservationService.get(reservationId);
        ReviewCreateRequest request = new ReviewCreateRequest();
        request.setReservationId(reservationId);

        model.addAttribute("reservation", reservation);
        model.addAttribute("reviewCreateRequest", request);
        model.addAttribute("review", reviewService.findByReservationId(reservationId).orElse(null));
        return "review/new";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute ReviewCreateRequest request,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        Reservation reservation = reservationService.get(request.getReservationId());

        if (bindingResult.hasErrors()) {
            model.addAttribute("reservation", reservation);
            model.addAttribute("review", reviewService.findByReservationId(request.getReservationId()).orElse(null));
            return "review/new";
        }

        try {
            reviewService.create(request);
            redirectAttributes.addFlashAttribute("reviewMessage", "리뷰가 등록되었습니다. 감사합니다.");
            return "redirect:/reservations/" + request.getReservationId();
        } catch (IllegalArgumentException exception) {
            model.addAttribute("reservation", reservation);
            model.addAttribute("review", reviewService.findByReservationId(request.getReservationId()).orElse(null));
            model.addAttribute("reviewError", exception.getMessage());
            return "review/new";
        }
    }
}
