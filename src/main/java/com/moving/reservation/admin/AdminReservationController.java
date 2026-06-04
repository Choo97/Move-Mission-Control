package com.moving.reservation.admin;

import com.moving.reservation.notification.CustomerNotificationService;
import com.moving.reservation.notification.EmailNotificationSendResult;
import com.moving.reservation.reservation.Reservation;
import com.moving.reservation.reservation.ReservationService;
import com.moving.reservation.reservation.ReservationStatus;
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

    public AdminReservationController(ReservationService reservationService,
                                      CustomerNotificationService customerNotificationService,
                                      ReviewService reviewService,
                                      EstimateDocumentPdfService estimateDocumentPdfService) {
        this.reservationService = reservationService;
        this.customerNotificationService = customerNotificationService;
        this.reviewService = reviewService;
        this.estimateDocumentPdfService = estimateDocumentPdfService;
    }

    @GetMapping
    public String list(@RequestParam(required = false) ReservationStatus status,
                       @RequestParam(required = false) String keyword,
                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                       Model model) {
        LocalDate currentDate = LocalDate.now();

        model.addAttribute("reservations", reservationService.search(status, keyword, startDate, endDate));
        model.addAttribute("summary", reservationService.summary());
        model.addAttribute("recentReservations", reservationService.findRecent());
        model.addAttribute("recentReviews", reviewService.findRecent());
        model.addAttribute("failedEmailCount", customerNotificationService.countFailedEmails());
        model.addAttribute("statuses", ReservationStatus.values());
        model.addAttribute("selectedStatus", status);
        model.addAttribute("keyword", keyword);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("today", currentDate);
        model.addAttribute("weekStart", currentDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)));
        model.addAttribute("weekEnd", currentDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)));
        model.addAttribute("monthStart", currentDate.withDayOfMonth(1));
        model.addAttribute("monthEnd", currentDate.with(TemporalAdjusters.lastDayOfMonth()));
        return "admin/reservations";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Reservation reservation = reservationService.get(id);
        model.addAttribute("reservation", reservation);
        model.addAttribute("estimateLines", reservationService.estimateLines(reservation));
        model.addAttribute("photos", reservationService.findPhotos(id));
        model.addAttribute("statusHistories", reservationService.findStatusHistories(id));
        model.addAttribute("notifications", customerNotificationService.findByReservationId(id));
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
    public String updateStatus(@PathVariable Long id, @RequestParam ReservationStatus status, Principal principal) {
        reservationService.updateStatus(id, status, principal.getName());
        return "redirect:/admin/reservations/" + id;
    }

    @PostMapping("/{id}/estimate")
    public String updateEstimate(@PathVariable Long id, @RequestParam Integer estimatedPrice) {
        reservationService.updateEstimate(id, estimatedPrice);
        return "redirect:/admin/reservations/" + id;
    }

    @PostMapping("/{id}/distance")
    public String updateDistance(@PathVariable Long id, @RequestParam(required = false) Integer distanceKm) {
        reservationService.updateDistance(id, distanceKm);
        return "redirect:/admin/reservations/" + id;
    }

    @PostMapping("/{id}/distance/calculate")
    public String calculateDistance(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            int distanceKm = reservationService.calculateAndUpdateDistance(id);
            redirectAttributes.addFlashAttribute("distanceMessage", distanceKm + "km 이동 거리를 자동 계산해 견적에 반영했습니다.");
        } catch (IllegalArgumentException | IllegalStateException exception) {
            redirectAttributes.addFlashAttribute("distanceError", exception.getMessage());
        }

        return "redirect:/admin/reservations/" + id;
    }

    @PostMapping("/{id}/memo")
    public String updateAdminMemo(@PathVariable Long id,
                                  @RequestParam(required = false) String adminMemo,
                                  Principal principal) {
        reservationService.updateAdminMemo(id, adminMemo, principal.getName());
        return "redirect:/admin/reservations/" + id;
    }

    @PostMapping("/{id}/notifications/email/send")
    public String sendReadyEmails(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        EmailNotificationSendResult result = customerNotificationService.sendReadyEmails(id);

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
    public String resendFailedEmails(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        EmailNotificationSendResult result = customerNotificationService.resendFailedEmails(id);

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
