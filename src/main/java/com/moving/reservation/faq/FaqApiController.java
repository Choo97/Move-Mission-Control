package com.moving.reservation.faq;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/faqs")
public class FaqApiController {
    private final FaqService faqService;

    public FaqApiController(FaqService faqService) {
        this.faqService = faqService;
    }

    @GetMapping
    public List<FaqResponse> activeFaqs() {
        return faqService.findActive().stream().map(FaqResponse::from).toList();
    }
}
