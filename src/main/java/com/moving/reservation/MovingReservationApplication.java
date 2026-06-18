package com.moving.reservation;

import com.moving.reservation.config.CorsProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(CorsProperties.class)
public class MovingReservationApplication {

    public static void main(String[] args) {
        SpringApplication.run(MovingReservationApplication.class, args);
    }
}
