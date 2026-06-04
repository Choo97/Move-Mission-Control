package com.moving.reservation.home;

import com.moving.reservation.faq.FaqService;
import com.moving.reservation.reservation.ReservationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final ReservationService reservationService;
    private final FaqService faqService;

    public HomeController(ReservationService reservationService, FaqService faqService) {
        this.reservationService = reservationService;
        this.faqService = faqService;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("summary", reservationService.summary());
        return "home";
    }

    @GetMapping("/faq")
    public String faq(Model model) {
        model.addAttribute("faqs", faqService.findActive());
        return "faq";
    }
}
