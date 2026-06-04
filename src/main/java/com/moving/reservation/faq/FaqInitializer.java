package com.moving.reservation.faq;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class FaqInitializer implements CommandLineRunner {

    private final FaqService faqService;

    public FaqInitializer(FaqService faqService) {
        this.faqService = faqService;
    }

    @Override
    public void run(String... args) {
        faqService.initializeDefaults();
    }
}
