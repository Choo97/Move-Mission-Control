package com.moving.reservation.reservation;

import java.util.ArrayList;
import java.util.List;
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
        return calculateLines(
                moveType,
                fromElevator,
                toElevator,
                fromFloor,
                toFloor,
                fromLadderTruck,
                toLadderTruck,
                distanceKm
        ).stream()
                .mapToInt(ReservationEstimateLine::amount)
                .sum();
    }

    public List<ReservationEstimateLine> calculateLines(MoveType moveType,
                                                        boolean fromElevator,
                                                        boolean toElevator,
                                                        Integer fromFloor,
                                                        Integer toFloor,
                                                        boolean fromLadderTruck,
                                                        boolean toLadderTruck,
                                                        Integer distanceKm) {
        List<ReservationEstimateLine> lines = new ArrayList<>();
        lines.add(new ReservationEstimateLine(moveType.getLabel() + " 기본가", moveType.getBasePrice()));
        addLocationLines(lines, "출발지", fromElevator, normalizeFloor(fromFloor), fromLadderTruck);
        addLocationLines(lines, "도착지", toElevator, normalizeFloor(toFloor), toLadderTruck);
        lines.add(new ReservationEstimateLine(distanceLabel(distanceKm), calculateDistanceSurcharge(distanceKm)));
        return lines;
    }

    private void addLocationLines(List<ReservationEstimateLine> lines, String location,
                                  boolean elevator, int floor, boolean ladderTruck) {
        if (ladderTruck) {
            lines.add(new ReservationEstimateLine(location + " 사다리차", LADDER_TRUCK_SURCHARGE));
        }

        if (!elevator) {
            lines.add(new ReservationEstimateLine(location + " 엘리베이터 없음", NO_ELEVATOR_SURCHARGE));

            if (!ladderTruck && floor >= FLOOR_SURCHARGE_START) {
                int extraFloorCount = floor - FLOOR_SURCHARGE_START + 1;
                lines.add(new ReservationEstimateLine(
                        location + " 고층 작업 추가요금 (" + floor + "층)",
                        extraFloorCount * FLOOR_SURCHARGE
                ));
            }
        }
    }

    private int calculateDistanceSurcharge(Integer distanceKm) {
        if (distanceKm == null || distanceKm <= INCLUDED_DISTANCE_KM) {
            return 0;
        }

        return (distanceKm - INCLUDED_DISTANCE_KM) * DISTANCE_SURCHARGE_PER_KM;
    }

    private String distanceLabel(Integer distanceKm) {
        if (distanceKm == null) {
            return "이동 거리 추가요금 (거리 확인 전)";
        }

        if (distanceKm <= INCLUDED_DISTANCE_KM) {
            return "이동 거리 추가요금 (" + distanceKm + "km, 10km까지 포함)";
        }

        return "이동 거리 추가요금 (" + distanceKm + "km, 초과 " + (distanceKm - INCLUDED_DISTANCE_KM) + "km)";
    }

    private int normalizeFloor(Integer floor) {
        if (floor == null || floor < 1) {
            return 1;
        }

        return floor;
    }
}
