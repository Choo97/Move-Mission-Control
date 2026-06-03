package com.moving.reservation.estimate;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class EstimateSettingInitializer implements CommandLineRunner {

    private final EstimateSettingService estimateSettingService;

    public EstimateSettingInitializer(EstimateSettingService estimateSettingService) {
        this.estimateSettingService = estimateSettingService;
    }

    @Override
    public void run(String... args) {
        estimateSettingService.initializeDefaults();
    }
}
