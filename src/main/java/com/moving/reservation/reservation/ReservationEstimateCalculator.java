package com.moving.reservation.reservation;

import org.springframework.stereotype.Component;

@Component
public class ReservationEstimateCalculator {

    private static final int NO_ELEVATOR_SURCHARGE = 50000;

    public int calculate(MoveType moveType, boolean fromElevator, boolean toElevator) {
        int estimate = moveType.getBasePrice();

        if (!fromElevator) {
            estimate += NO_ELEVATOR_SURCHARGE;
        }

        if (!toElevator) {
            estimate += NO_ELEVATOR_SURCHARGE;
        }

        return estimate;
    }
}
