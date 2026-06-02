package com.moving.reservation.reservation;

import org.springframework.stereotype.Component;

@Component
public class ReservationEstimateCalculator {

    private static final int NO_ELEVATOR_SURCHARGE = 50000;
    private static final int FLOOR_SURCHARGE = 20000;
    private static final int LADDER_TRUCK_SURCHARGE = 120000;
    private static final int FLOOR_SURCHARGE_START = 3;

    public int calculate(MoveType moveType,
                         boolean fromElevator,
                         boolean toElevator,
                         Integer fromFloor,
                         Integer toFloor,
                         boolean fromLadderTruck,
                         boolean toLadderTruck) {
        int estimate = moveType.getBasePrice();

        estimate += calculateLocationSurcharge(fromElevator, normalizeFloor(fromFloor), fromLadderTruck);
        estimate += calculateLocationSurcharge(toElevator, normalizeFloor(toFloor), toLadderTruck);

        return estimate;
    }

    private int calculateLocationSurcharge(boolean elevator, int floor, boolean ladderTruck) {
        int surcharge = ladderTruck ? LADDER_TRUCK_SURCHARGE : 0;

        if (!elevator) {
            surcharge += NO_ELEVATOR_SURCHARGE;

            if (!ladderTruck && floor >= FLOOR_SURCHARGE_START) {
                surcharge += (floor - FLOOR_SURCHARGE_START + 1) * FLOOR_SURCHARGE;
            }
        }

        return surcharge;
    }

    private int normalizeFloor(Integer floor) {
        if (floor == null || floor < 1) {
            return 1;
        }

        return floor;
    }
}
