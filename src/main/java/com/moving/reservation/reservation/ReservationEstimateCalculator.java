package com.moving.reservation.reservation;

import org.springframework.stereotype.Component;

@Component
public class ReservationEstimateCalculator {

    private static final int NO_ELEVATOR_SURCHARGE = 50000;
    private static final int FLOOR_SURCHARGE = 20000;
    private static final int LADDER_TRUCK_SURCHARGE = 120000;
    private static final int FLOOR_SURCHARGE_START = 3;
    private static final int INCLUDED_DISTANCE_KM = 10;
    private static final int DISTANCE_SURCHARGE_PER_KM = 10000;

    public int calculate(MoveType moveType,
                         boolean fromElevator,
                         boolean toElevator,
                         Integer fromFloor,
                         Integer toFloor,
                         boolean fromLadderTruck,
                         boolean toLadderTruck,
                         Integer distanceKm) {
        int estimate = moveType.getBasePrice();

        estimate += calculateLocationSurcharge(fromElevator, normalizeFloor(fromFloor), fromLadderTruck);
        estimate += calculateLocationSurcharge(toElevator, normalizeFloor(toFloor), toLadderTruck);
        estimate += calculateDistanceSurcharge(distanceKm);

        return estimate;
    }

    private int calculateDistanceSurcharge(Integer distanceKm) {
        if (distanceKm == null || distanceKm <= INCLUDED_DISTANCE_KM) {
            return 0;
        }

        return (distanceKm - INCLUDED_DISTANCE_KM) * DISTANCE_SURCHARGE_PER_KM;
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
