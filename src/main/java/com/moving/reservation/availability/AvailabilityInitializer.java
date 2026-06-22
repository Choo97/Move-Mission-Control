package com.moving.reservation.availability;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class AvailabilityInitializer implements ApplicationRunner {
    private final AvailabilityService availabilityService;

    public AvailabilityInitializer(AvailabilityService availabilityService) {
        this.availabilityService = availabilityService;
    }

    @Override
    public void run(ApplicationArguments args) {
        availabilityService.initializeDefaults();
    }
}
