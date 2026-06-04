package com.moving.reservation.home;

import com.moving.reservation.reservation.ReservationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final ReservationService reservationService;

    public HomeController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("summary", reservationService.summary());
        return "home";
    }

    @GetMapping("/faq")
    public String faq() {
        return "faq";
    }
}
