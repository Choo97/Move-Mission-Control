package com.moving.reservation.reservation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "고객 예약 상세 응답")
public record ReservationApiResponse(
        @Schema(description = "예약 번호", example = "1")
        Long id,
        @Schema(description = "예약자 이름", example = "홍길동")
        String customerName,
        @Schema(description = "예약자 이메일", example = "customer@example.com")
        String email,
        @Schema(description = "이사 날짜", example = "2026-07-01")
        LocalDate moveDate,
        @Schema(description = "이사 시간", example = "10:30:00")
        LocalTime moveTime,
        @Schema(description = "출발지 주소", example = "서울시 강남구 테헤란로 1")
        String fromAddress,
        @Schema(description = "도착지 주소", example = "서울시 송파구 올림픽로 1")
        String toAddress,
        @Schema(description = "이사 유형 코드", example = "STUDIO")
        String moveType,
        @Schema(description = "이사 유형 한글명", example = "원룸")
        String moveTypeLabel,
        @Schema(description = "예약 상태 코드", example = "RECEIVED")
        String status,
        @Schema(description = "예약 상태 한글명", example = "접수")
        String statusLabel,
        @Schema(description = "출발지 엘리베이터 사용 가능 여부", example = "true")
        boolean fromElevator,
        @Schema(description = "도착지 엘리베이터 사용 가능 여부", example = "true")
        boolean toElevator,
        @Schema(description = "출발지 층수", example = "3")
        Integer fromFloor,
        @Schema(description = "도착지 층수", example = "5")
        Integer toFloor,
        @Schema(description = "출발지 사다리차 필요 여부", example = "false")
        boolean fromLadderTruck,
        @Schema(description = "도착지 사다리차 필요 여부", example = "false")
        boolean toLadderTruck,
        @Schema(description = "출발지와 도착지 사이 거리 km", example = "7")
        Integer distanceKm,
        @Schema(description = "할인 적용 전 기본 견적 금액", example = "250000")
        Integer estimatedPrice,
        @Schema(description = "쿠폰 할인 전 기준 견적 금액", example = "250000")
        Integer baseEstimatedPrice,
        @Schema(description = "적용된 할인 금액", example = "10000")
        Integer discountAmount,
        @Schema(description = "최종 견적 금액", example = "240000")
        Integer finalEstimatedPrice,
        @Schema(description = "적용된 쿠폰 코드", example = "WELCOME10")
        String couponCode,
        @Schema(description = "적용된 쿠폰 이름", example = "첫 예약 할인")
        String couponName,
        @Schema(description = "고객이 예약 정보를 수정할 수 있는지 여부", example = "true")
        boolean editable,
        @Schema(description = "고객이 예약을 취소할 수 있는지 여부", example = "true")
        boolean cancelable,
        @Schema(description = "고객이 견적에 동의할 수 있는지 여부", example = "false")
        boolean estimateAcceptable,
        @Schema(description = "고객이 견적에 이미 동의했는지 여부", example = "false")
        boolean estimateAccepted,
        @Schema(description = "고객이 동의한 견적 금액", example = "240000")
        Integer acceptedEstimatePrice,
        @Schema(description = "견적 동의 시각", example = "2026-06-18T16:30:00")
        LocalDateTime estimateAcceptedAt,
        @Schema(description = "고객이 업로드한 짐 사진 목록")
        List<ReservationApiPhotoResponse> photos,
        @Schema(description = "견적 산정 내역")
        List<ReservationApiEstimateLineResponse> estimateLines
) {

    public static ReservationApiResponse from(Reservation reservation, List<ReservationEstimateLine> estimateLines) {
        return from(reservation, estimateLines, List.of());
    }

    public static ReservationApiResponse from(Reservation reservation,
                                              List<ReservationEstimateLine> estimateLines,
                                              List<ReservationPhoto> photos) {
        return new ReservationApiResponse(
                reservation.getId(),
                reservation.getCustomerName(),
                reservation.getEmail(),
                reservation.getMoveDate(),
                reservation.getMoveTime(),
                reservation.getFromAddress(),
                reservation.getToAddress(),
                reservation.getMoveType().name(),
                reservation.getMoveType().getLabel(),
                reservation.getStatus().name(),
                reservation.getStatus().getLabel(),
                reservation.isFromElevator(),
                reservation.isToElevator(),
                reservation.getFromFloor(),
                reservation.getToFloor(),
                reservation.isFromLadderTruck(),
                reservation.isToLadderTruck(),
                reservation.getDistanceKm(),
                reservation.getEstimatedPrice(),
                reservation.getBaseEstimatedPrice(),
                reservation.getAppliedDiscountAmount(),
                reservation.getFinalEstimatedPrice(),
                reservation.getCouponCode(),
                reservation.getCouponName(),
                reservation.isEditable(),
                reservation.isCancelable(),
                reservation.isEstimateAcceptable(),
                reservation.hasEstimateAcceptance(),
                reservation.getAcceptedEstimatePrice(),
                reservation.getEstimateAcceptedAt(),
                photos.stream()
                        .map(ReservationApiPhotoResponse::from)
                        .toList(),
                estimateLines.stream()
                        .map(ReservationApiEstimateLineResponse::from)
                        .toList()
        );
    }
}
