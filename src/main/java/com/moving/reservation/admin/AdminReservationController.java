package com.moving.reservation.admin;

import com.moving.reservation.auth.AdminAccountService;
import com.moving.reservation.notification.CustomerNotificationService;
import com.moving.reservation.notification.EmailNotificationSendResult;
import com.moving.reservation.reservation.Reservation;
import com.moving.reservation.reservation.ReservationService;
import com.moving.reservation.reservation.ReservationSort;
import com.moving.reservation.reservation.ReservationStatus;
import com.moving.reservation.reservation.ReservationSummary;
import com.moving.reservation.review.ReviewService;
import java.security.Principal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/reservations")
public class AdminReservationController {

    private final ReservationService reservationService;
    private final CustomerNotificationService customerNotificationService;
    private final ReviewService reviewService;
    private final EstimateDocumentPdfService estimateDocumentPdfService;
    private final AdminAccountService adminAccountService;
    private final AdminAuditLogService adminAuditLogService;

    public AdminReservationController(ReservationService reservationService,
                                      CustomerNotificationService customerNotificationService,
                                      ReviewService reviewService,
                                      EstimateDocumentPdfService estimateDocumentPdfService,
                                      AdminAccountService adminAccountService,
                                      AdminAuditLogService adminAuditLogService) {
        this.reservationService = reservationService;
        this.customerNotificationService = customerNotificationService;
        this.reviewService = reviewService;
        this.estimateDocumentPdfService = estimateDocumentPdfService;
        this.adminAccountService = adminAccountService;
        this.adminAuditLogService = adminAuditLogService;
    }

    @GetMapping
    public String list(@RequestParam(required = false) ReservationStatus status,
                       @RequestParam(required = false) String keyword,
                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                       @RequestParam(required = false) Boolean needsDistance,
                       @RequestParam(required = false) ReservationSort sort,
                       Principal principal,
                       Model model) {
        LocalDate currentDate = LocalDate.now();
        long failedEmailCount = customerNotificationService.countFailedEmails();
        ReservationSummary summary = reservationService.summary();

        ReservationSort selectedSort = sort == null ? ReservationSort.PRIORITY : sort;

        model.addAttribute("reservations", reservationService.search(status, keyword, startDate, endDate, needsDistance, selectedSort));
        model.addAttribute("summary", summary);
        model.addAttribute("recentReservations", reservationService.findRecent());
        model.addAttribute("recentReviews", reviewService.findRecent());
        model.addAttribute("failedEmailCount", failedEmailCount);
        model.addAttribute("taskSummary", new AdminDashboardTaskSummary(
                summary.received(),
                summary.consulting(),
                reservationService.countEstimateAcceptancePending(),
                reservationService.countDistancePending(),
                failedEmailCount
        ));
        model.addAttribute("statuses", ReservationStatus.values());
        model.addAttribute("sorts", ReservationSort.values());
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedSort", selectedSort);
        model.addAttribute("keyword", keyword);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("needsDistance", Boolean.TRUE.equals(needsDistance));
        model.addAttribute("today", currentDate);
        model.addAttribute("weekStart", currentDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)));
        model.addAttribute("weekEnd", currentDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)));
        model.addAttribute("monthStart", currentDate.withDayOfMonth(1));
        model.addAttribute("monthEnd", currentDate.with(TemporalAdjusters.lastDayOfMonth()));
        model.addAttribute("usesUnsafeDefaultPassword",
                principal != null && adminAccountService.usesUnsafeDefaultPassword(principal.getName()));
        return "admin/reservations";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Reservation reservation = reservationService.get(id);
        model.addAttribute("reservation", reservation);
        model.addAttribute("today", LocalDate.now());
        model.addAttribute("estimateLines", reservationService.estimateLines(reservation));
        model.addAttribute("photos", reservationService.findPhotos(id));
        model.addAttribute("statusHistories", reservationService.findStatusHistories(id));
        model.addAttribute("notifications", customerNotificationService.findByReservationId(id));
        model.addAttribute("auditLogs", adminAuditLogService.findByReservationId(id));
        model.addAttribute("statuses", ReservationStatus.values());
        return "admin/reservation-detail";
    }

    @GetMapping("/{id}/estimate-document")
    public String estimateDocument(@PathVariable Long id, Model model) {
        Reservation reservation = reservationService.get(id);
        model.addAttribute("reservation", reservation);
        model.addAttribute("estimateLines", reservationService.estimateLines(reservation));
        return "admin/estimate-document";
    }

    @GetMapping("/{id}/estimate-document.pdf")
    public ResponseEntity<byte[]> estimateDocumentPdf(@PathVariable Long id) {
        Reservation reservation = reservationService.get(id);
        byte[] pdf = estimateDocumentPdfService.generate(reservation, reservationService.estimateLines(reservation));
        String filename = "estimate-" + id + ".pdf";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(filename)
                        .build()
                        .toString())
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable Long id,
                               @RequestParam ReservationStatus status,
                               Principal principal,
                               RedirectAttributes redirectAttributes) {
        try {
            Reservation reservation = reservationService.get(id);
            reservationService.updateStatus(id, status, principal.getName());
            adminAuditLogService.record(
                    reservation,
                    "예약 상태 변경",
                    "예약 상태를 '" + status.getLabel() + "'(으)로 변경했습니다.",
                    principal.getName()
            );
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("statusError", exception.getMessage());
        }
        return "redirect:/admin/reservations/" + id;
    }

    @PostMapping("/{id}/estimate")
    public String updateEstimate(@PathVariable Long id, @RequestParam Integer estimatedPrice, Principal principal) {
        Reservation reservation = reservationService.get(id);
        reservationService.updateEstimate(id, estimatedPrice);
        adminAuditLogService.record(
                reservation,
                "견적 금액 저장",
                "견적 금액을 " + String.format("%,d", estimatedPrice) + "원으로 저장했습니다.",
                principal.getName()
        );
        return "redirect:/admin/reservations/" + id;
    }

    @PostMapping("/{id}/distance")
    public String updateDistance(@PathVariable Long id,
                                 @RequestParam(required = false) Integer distanceKm,
                                 Principal principal) {
        Reservation reservation = reservationService.get(id);
        reservationService.updateDistance(id, distanceKm);
        adminAuditLogService.record(
                reservation,
                "이동 거리 저장",
                distanceKm == null ? "이동 거리를 확인 전으로 저장했습니다." : "이동 거리를 " + distanceKm + "km로 저장했습니다.",
                principal.getName()
        );
        return "redirect:/admin/reservations/" + id;
    }

    @PostMapping("/{id}/distance/calculate")
    public String calculateDistance(@PathVariable Long id, Principal principal, RedirectAttributes redirectAttributes) {
        Reservation reservation = reservationService.get(id);
        try {
            int distanceKm = reservationService.calculateAndUpdateDistance(id);
            redirectAttributes.addFlashAttribute("distanceMessage", distanceKm + "km 이동 거리를 자동 계산해 견적에 반영했습니다.");
            adminAuditLogService.record(
                    reservation,
                    "이동 거리 자동 계산",
                    "지도 API로 이동 거리를 " + distanceKm + "km로 계산해 저장했습니다.",
                    principal.getName()
            );
        } catch (IllegalArgumentException | IllegalStateException exception) {
            redirectAttributes.addFlashAttribute("distanceError", exception.getMessage());
        }

        return "redirect:/admin/reservations/" + id;
    }

    @PostMapping("/{id}/memo")
    public String updateAdminMemo(@PathVariable Long id,
                                  @RequestParam(required = false) String adminMemo,
                                  Principal principal) {
        Reservation reservation = reservationService.get(id);
        reservationService.updateAdminMemo(id, adminMemo, principal.getName());
        adminAuditLogService.record(
                reservation,
                "관리자 메모 저장",
                "관리자 메모를 저장했습니다.",
                principal.getName()
        );
        return "redirect:/admin/reservations/" + id;
    }

    @PostMapping("/{id}/notifications/email/send")
    public String sendReadyEmails(@PathVariable Long id, Principal principal, RedirectAttributes redirectAttributes) {
        Reservation reservation = reservationService.get(id);
        EmailNotificationSendResult result = customerNotificationService.sendReadyEmails(id);
        adminAuditLogService.record(
                reservation,
                "준비 이메일 발송",
                result.sentCount() + "건 발송, " + result.failedCount() + "건 실패로 처리했습니다.",
                principal.getName()
        );

        if (result.sentCount() > 0) {
            redirectAttributes.addFlashAttribute("emailSendMessage", result.sentCount() + "건의 이메일을 발송했습니다.");
        }

        if (result.failedCount() > 0) {
            redirectAttributes.addFlashAttribute("emailSendError", result.failedCount() + "건의 이메일 발송에 실패했습니다. 알림 이력을 확인해 주세요.");
        }

        if (result.sentCount() == 0 && result.failedCount() == 0) {
            redirectAttributes.addFlashAttribute("emailSendMessage", "발송 준비 상태의 이메일 알림이 없습니다.");
        }

        return "redirect:/admin/reservations/" + id;
    }

    @PostMapping("/{id}/notifications/email/resend-failed")
    public String resendFailedEmails(@PathVariable Long id, Principal principal, RedirectAttributes redirectAttributes) {
        Reservation reservation = reservationService.get(id);
        EmailNotificationSendResult result = customerNotificationService.resendFailedEmails(id);
        adminAuditLogService.record(
                reservation,
                "실패 이메일 재발송",
                result.sentCount() + "건 재발송, " + result.failedCount() + "건 실패로 처리했습니다.",
                principal.getName()
        );

        if (result.sentCount() > 0) {
            redirectAttributes.addFlashAttribute("emailSendMessage", result.sentCount() + "건의 실패 이메일을 재발송했습니다.");
        }

        if (result.failedCount() > 0) {
            redirectAttributes.addFlashAttribute("emailSendError", result.failedCount() + "건의 실패 이메일 재발송에 실패했습니다. 알림 이력을 확인해 주세요.");
        }

        if (result.sentCount() == 0 && result.failedCount() == 0) {
            redirectAttributes.addFlashAttribute("emailSendMessage", "재발송할 실패 이메일 알림이 없습니다.");
        }

        return "redirect:/admin/reservations/" + id;
    }
}
