package com.moving.reservation.admin;

import com.moving.reservation.reservation.Reservation;
import com.moving.reservation.reservation.ReservationApiEstimateLineResponse;
import com.moving.reservation.reservation.ReservationApiPhotoResponse;
import com.moving.reservation.reservation.ReservationCustomerActionHistory;
import com.moving.reservation.reservation.ReservationCustomerRequest;
import com.moving.reservation.reservation.ReservationCustomerRequestResponse;
import com.moving.reservation.reservation.ReservationEstimateLine;
import com.moving.reservation.reservation.ReservationPhoto;
import com.moving.reservation.reservation.ReservationStatusHistory;
import com.moving.reservation.notification.CustomerNotification;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public record AdminReservationDetailResponse(
        Long id,
        String customerName,
        String phone,
        String email,
        LocalDate moveDate,
        LocalTime moveTime,
        String fromAddress,
        String toAddress,
        String moveType,
        String moveTypeLabel,
        String serviceType,
        String serviceTypeLabel,
        String status,
        String statusLabel,
        List<AdminReservationStatusOptionResponse> selectableStatuses,
        boolean fromElevator,
        boolean toElevator,
        Integer fromFloor,
        Integer toFloor,
        boolean fromLadderTruck,
        boolean toLadderTruck,
        Integer distanceKm,
        Integer estimatedPrice,
        Integer baseEstimatedPrice,
        Integer discountAmount,
        Integer finalEstimatedPrice,
        String couponCode,
        String couponName,
        String memo,
        String adminMemo,
        String adminMemoUpdatedBy,
        boolean editable,
        boolean cancelable,
        boolean estimateAcceptable,
        boolean estimateAccepted,
        Integer acceptedEstimatePrice,
        LocalDateTime estimateAcceptedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<ReservationApiPhotoResponse> photos,
        List<ReservationApiEstimateLineResponse> estimateLines,
        List<AdminReservationStatusHistoryResponse> statusHistories,
        List<AdminReservationCustomerActionHistoryResponse> customerActionHistories,
        List<ReservationCustomerRequestResponse> customerRequests,
        List<AdminNotificationHistoryResponse> notifications,
        List<AdminAuditLogResponse> auditLogs
) {

    public static AdminReservationDetailResponse from(Reservation reservation,
                                                      List<ReservationEstimateLine> estimateLines,
                                                      List<ReservationPhoto> photos,
                                                      List<ReservationStatusHistory> statusHistories,
                                                      List<ReservationCustomerActionHistory> customerActionHistories,
                                                      List<ReservationCustomerRequest> customerRequests,
                                                      List<CustomerNotification> notifications,
                                                      List<AdminAuditLog> auditLogs) {
        return new AdminReservationDetailResponse(
                reservation.getId(),
                reservation.getCustomerName(),
                reservation.getPhone(),
                reservation.getEmail(),
                reservation.getMoveDate(),
                reservation.getMoveTime(),
                reservation.getFromAddress(),
                reservation.getToAddress(),
                reservation.getMoveType().name(),
                reservation.getMoveType().getLabel(),
                reservation.getServiceType().name(),
                reservation.getServiceType().getLabel(),
                reservation.getStatus().name(),
                reservation.getStatus().getLabel(),
                reservation.getStatus().getSelectableStatuses().stream()
                        .map(status -> AdminReservationStatusOptionResponse.from(status, reservation.getStatus()))
                        .toList(),
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
                reservation.getMemo(),
                reservation.getAdminMemo(),
                reservation.getAdminMemoUpdatedBy(),
                reservation.isEditable(),
                reservation.isCancelable(),
                reservation.isEstimateAcceptable(),
                reservation.hasEstimateAcceptance(),
                reservation.getAcceptedEstimatePrice(),
                reservation.getEstimateAcceptedAt(),
                reservation.getCreatedAt(),
                reservation.getUpdatedAt(),
                photos.stream()
                        .map(ReservationApiPhotoResponse::from)
                        .toList(),
                estimateLines.stream()
                        .map(ReservationApiEstimateLineResponse::from)
                        .toList(),
                statusHistories.stream()
                        .map(AdminReservationStatusHistoryResponse::from)
                        .toList(),
                customerActionHistories.stream()
                        .map(AdminReservationCustomerActionHistoryResponse::from)
                        .toList(),
                customerRequests.stream()
                        .map(ReservationCustomerRequestResponse::from)
                        .toList(),
                notifications.stream()
                        .map(AdminNotificationHistoryResponse::from)
                        .toList(),
                auditLogs.stream()
                        .map(AdminAuditLogResponse::from)
                        .toList()
        );
    }
}
