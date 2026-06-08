package com.moving.reservation.reservation;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class CustomerGuideInitializer implements ApplicationRunner {

    private final CustomerGuideService customerGuideService;

    public CustomerGuideInitializer(CustomerGuideService customerGuideService) {
        this.customerGuideService = customerGuideService;
    }

    @Override
    public void run(ApplicationArguments args) {
        customerGuideService.initializeDefaults();
    }
}
