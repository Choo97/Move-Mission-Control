package com.moving.reservation.reservation;

import com.moving.reservation.estimate.EstimateSettingKey;
import com.moving.reservation.estimate.EstimateSettingService;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ReservationEstimateCalculator {

    private static final int FLOOR_SURCHARGE_START = 3;

    private final EstimateSettingService estimateSettingService;

    public ReservationEstimateCalculator(EstimateSettingService estimateSettingService) {
        this.estimateSettingService = estimateSettingService;
    }

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
        lines.add(new ReservationEstimateLine(moveType.getLabel() + " 기본가", moveTypeBasePrice(moveType)));
        addLocationLines(lines, "출발지", fromElevator, normalizeFloor(fromFloor), fromLadderTruck);
        addLocationLines(lines, "도착지", toElevator, normalizeFloor(toFloor), toLadderTruck);
        lines.add(new ReservationEstimateLine(distanceLabel(distanceKm), calculateDistanceSurcharge(distanceKm)));
        return lines;
    }

    private void addLocationLines(List<ReservationEstimateLine> lines, String location,
                                  boolean elevator, int floor, boolean ladderTruck) {
        if (ladderTruck) {
            lines.add(new ReservationEstimateLine(location + " 사다리차",
                    estimateSettingService.amount(EstimateSettingKey.LADDER_TRUCK_SURCHARGE)));
        }

        if (!elevator) {
            lines.add(new ReservationEstimateLine(location + " 엘리베이터 없음",
                    estimateSettingService.amount(EstimateSettingKey.NO_ELEVATOR_SURCHARGE)));

            if (!ladderTruck && floor >= FLOOR_SURCHARGE_START) {
                int extraFloorCount = floor - FLOOR_SURCHARGE_START + 1;
                lines.add(new ReservationEstimateLine(
                        location + " 고층 작업 추가요금 (" + floor + "층)",
                        extraFloorCount * estimateSettingService.amount(EstimateSettingKey.FLOOR_SURCHARGE)
                ));
            }
        }
    }

    private int calculateDistanceSurcharge(Integer distanceKm) {
        int includedDistanceKm = estimateSettingService.amount(EstimateSettingKey.INCLUDED_DISTANCE_KM);
        if (distanceKm == null || distanceKm <= includedDistanceKm) {
            return 0;
        }

        return (distanceKm - includedDistanceKm) * estimateSettingService.amount(EstimateSettingKey.DISTANCE_SURCHARGE_PER_KM);
    }

    private String distanceLabel(Integer distanceKm) {
        int includedDistanceKm = estimateSettingService.amount(EstimateSettingKey.INCLUDED_DISTANCE_KM);
        if (distanceKm == null) {
            return "이동 거리 추가요금 (거리 확인 전)";
        }

        if (distanceKm <= includedDistanceKm) {
            return "이동 거리 추가요금 (" + distanceKm + "km, " + includedDistanceKm + "km까지 포함)";
        }

        return "이동 거리 추가요금 (" + distanceKm + "km, 초과 " + (distanceKm - includedDistanceKm) + "km)";
    }

    private int moveTypeBasePrice(MoveType moveType) {
        return switch (moveType) {
            case STUDIO -> estimateSettingService.amount(EstimateSettingKey.STUDIO_BASE);
            case TWO_ROOM -> estimateSettingService.amount(EstimateSettingKey.TWO_ROOM_BASE);
            case FAMILY -> estimateSettingService.amount(EstimateSettingKey.FAMILY_BASE);
            case OFFICE -> estimateSettingService.amount(EstimateSettingKey.OFFICE_BASE);
            case STORAGE -> estimateSettingService.amount(EstimateSettingKey.STORAGE_BASE);
        };
    }

    private int normalizeFloor(Integer floor) {
        if (floor == null || floor < 1) {
            return 1;
        }

        return floor;
    }
}
