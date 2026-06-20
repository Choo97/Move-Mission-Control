package com.moving.reservation.admin;

import com.moving.reservation.reservation.Reservation;
import com.moving.reservation.reservation.ReservationService;
import com.moving.reservation.reservation.ReservationSort;
import com.moving.reservation.reservation.ReservationStatus;
import com.moving.reservation.notification.CustomerNotificationService;
import com.moving.reservation.notification.EmailNotificationSendResult;
import java.time.LocalDate;
import java.util.List;
import java.security.Principal;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/reservations")
public class AdminReservationApiController {

    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final List<Integer> ALLOWED_PAGE_SIZES = List.of(10, 20, 50);

    private final ReservationService reservationService;
    private final AdminAuditLogService adminAuditLogService;
    private final CustomerNotificationService customerNotificationService;

    public AdminReservationApiController(ReservationService reservationService,
                                         AdminAuditLogService adminAuditLogService,
                                         CustomerNotificationService customerNotificationService) {
        this.reservationService = reservationService;
        this.adminAuditLogService = adminAuditLogService;
        this.customerNotificationService = customerNotificationService;
    }

    @GetMapping
    public AdminReservationPageResponse list(@RequestParam(required = false) ReservationStatus status,
                                             @RequestParam(required = false) String keyword,
                                             @RequestParam(required = false)
                                             @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                             @RequestParam(required = false)
                                             @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                             @RequestParam(required = false) Boolean needsDistance,
                                             @RequestParam(required = false) ReservationSort sort,
                                             @RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "10") int size) {
        ReservationSort selectedSort = sort == null ? ReservationSort.PRIORITY : sort;
        Page<Reservation> reservationPage = reservationService.searchPage(
                status,
                keyword,
                startDate,
                endDate,
                needsDistance,
                selectedSort,
                PageRequest.of(Math.max(page, 0), selectedPageSize(size))
        );

        return AdminReservationPageResponse.from(reservationPage);
    }

    @GetMapping("/{id}")
    public AdminReservationDetailResponse detail(@PathVariable Long id) {
        Reservation reservation = reservationService.get(id);

        return detailResponse(reservation);
    }

    @PatchMapping("/{id}/status")
    public AdminReservationDetailResponse updateStatus(@PathVariable Long id,
                                                       @Valid @RequestBody AdminReservationStatusUpdateRequest request,
                                                       Principal principal) {
        reservationService.updateStatus(id, request.getStatus(), principal.getName());
        Reservation reservation = reservationService.get(id);

        return detailResponse(reservation);
    }

    @PatchMapping("/{id}/estimate")
    public AdminReservationDetailResponse updateEstimate(@PathVariable Long id,
                                                         @RequestBody AdminReservationEstimateUpdateRequest request,
                                                         Principal principal) {
        if (request.getEstimatedPrice() == null) {
            throw new IllegalArgumentException("견적 금액을 입력해 주세요.");
        }

        if (request.getEstimatedPrice() < 0) {
            throw new IllegalArgumentException("견적 금액은 0원 이상이어야 합니다.");
        }

        Reservation reservation = reservationService.get(id);
        reservationService.updateEstimate(id, request.getEstimatedPrice());
        adminAuditLogService.record(
                reservation,
                "견적 금액 저장",
                "견적 금액을 " + String.format("%,d", request.getEstimatedPrice()) + "원으로 저장했습니다.",
                principal.getName()
        );

        return detailResponse(reservation);
    }

    @PatchMapping("/{id}/distance")
    public AdminReservationDetailResponse updateDistance(@PathVariable Long id,
                                                         @RequestBody AdminReservationDistanceUpdateRequest request,
                                                         Principal principal) {
        if (request.getDistanceKm() != null && request.getDistanceKm() < 0) {
            throw new IllegalArgumentException("이동 거리는 0km 이상이어야 합니다.");
        }

        Reservation reservation = reservationService.get(id);
        reservationService.updateDistance(id, request.getDistanceKm());
        adminAuditLogService.record(
                reservation,
                "이동 거리 저장",
                request.getDistanceKm() == null
                        ? "이동 거리를 확인 전으로 저장했습니다."
                        : "이동 거리를 " + request.getDistanceKm() + "km로 저장했습니다.",
                principal.getName()
        );

        return detailResponse(reservation);
    }

    @PatchMapping("/{id}/distance/calculate")
    public AdminReservationDetailResponse calculateDistance(@PathVariable Long id, Principal principal) {
        Reservation reservation = reservationService.get(id);
        int distanceKm = reservationService.calculateAndUpdateDistance(id);
        adminAuditLogService.record(
                reservation,
                "이동 거리 자동 계산",
                "지도 API로 이동 거리를 " + distanceKm + "km로 계산해 저장했습니다.",
                principal.getName()
        );

        return detailResponse(reservation);
    }

    @PatchMapping("/{id}/memo")
    public AdminReservationDetailResponse updateMemo(@PathVariable Long id,
                                                     @RequestBody AdminReservationMemoUpdateRequest request,
                                                     Principal principal) {
        String adminMemo = request.getAdminMemo();

        if (adminMemo != null && adminMemo.length() > 1000) {
            throw new IllegalArgumentException("관리자 메모는 1,000자 이내로 입력해 주세요.");
        }

        Reservation reservation = reservationService.get(id);
        reservationService.updateAdminMemo(id, adminMemo, principal.getName());
        adminAuditLogService.record(
                reservation,
                "관리자 메모 저장",
                adminMemo == null || adminMemo.isBlank()
                        ? "관리자 메모를 비웠습니다."
                        : "관리자 메모를 저장했습니다.",
                principal.getName()
        );

        return detailResponse(reservation);
    }

    @PostMapping("/{id}/notifications/email/send")
    public AdminEmailSendResponse sendReadyEmails(@PathVariable Long id, Principal principal) {
        Reservation reservation = reservationService.get(id);
        EmailNotificationSendResult result = customerNotificationService.sendReadyEmails(id);
        adminAuditLogService.record(
                reservation,
                "준비 이메일 발송",
                result.sentCount() + "건 발송, " + result.failedCount() + "건 실패로 처리했습니다.",
                principal.getName()
        );

        return new AdminEmailSendResponse(
                result.sentCount(),
                result.failedCount(),
                detailResponse(reservation)
        );
    }

    @PostMapping("/{id}/notifications/email/resend-failed")
    public AdminEmailSendResponse resendFailedEmails(@PathVariable Long id, Principal principal) {
        Reservation reservation = reservationService.get(id);
        EmailNotificationSendResult result = customerNotificationService.resendFailedEmails(id);
        adminAuditLogService.record(
                reservation,
                "실패 이메일 재발송",
                result.sentCount() + "건 재발송, " + result.failedCount() + "건 실패로 처리했습니다.",
                principal.getName()
        );

        return new AdminEmailSendResponse(
                result.sentCount(),
                result.failedCount(),
                detailResponse(reservation)
        );
    }

    private int selectedPageSize(int size) {
        return ALLOWED_PAGE_SIZES.contains(size) ? size : DEFAULT_PAGE_SIZE;
    }

    private AdminReservationDetailResponse detailResponse(Reservation reservation) {
        return AdminReservationDetailResponse.from(
                reservation,
                reservationService.estimateLines(reservation),
                reservationService.findPhotos(reservation.getId()),
                reservationService.findStatusHistories(reservation.getId()),
                reservationService.findCustomerActionHistories(reservation.getId()),
                customerNotificationService.findByReservationId(reservation.getId()),
                adminAuditLogService.findByReservationId(reservation.getId())
        );
    }
}
