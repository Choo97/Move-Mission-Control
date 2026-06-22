package com.moving.reservation;

import com.moving.reservation.config.CorsProperties;
import com.moving.reservation.config.PrivacyRetentionProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties({CorsProperties.class, PrivacyRetentionProperties.class})
public class MovingReservationApplication {

    public static void main(String[] args) {
        SpringApplication.run(MovingReservationApplication.class, args);
    }
}
