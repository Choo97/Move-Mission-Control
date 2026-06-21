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
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.stream.IntStream;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;

@Controller
@RequestMapping("/admin/reservations")
public class AdminReservationController {

    private final ReservationService reservationService;
    private final CustomerNotificationService customerNotificationService;
    private final ReviewService reviewService;
    private final EstimateDocumentPdfService estimateDocumentPdfService;
    private final AdminAccountService adminAccountService;
    private final AdminAuditLogService adminAuditLogService;
    private final AdminReservationCsvExporter adminReservationCsvExporter;
    private static final int DEFAULT_RESERVATION_PAGE_SIZE = 10;
    private static final List<Integer> RESERVATION_PAGE_SIZES = List.of(10, 20, 50);

    public AdminReservationController(ReservationService reservationService,
                                      CustomerNotificationService customerNotificationService,
                                      ReviewService reviewService,
                                      EstimateDocumentPdfService estimateDocumentPdfService,
                                      AdminAccountService adminAccountService,
                                      AdminAuditLogService adminAuditLogService,
                                      AdminReservationCsvExporter adminReservationCsvExporter) {
        this.reservationService = reservationService;
        this.customerNotificationService = customerNotificationService;
        this.reviewService = reviewService;
        this.estimateDocumentPdfService = estimateDocumentPdfService;
        this.adminAccountService = adminAccountService;
        this.adminAuditLogService = adminAuditLogService;
        this.adminReservationCsvExporter = adminReservationCsvExporter;
    }

    @GetMapping
    public String list(@RequestParam(required = false) ReservationStatus status,
                       @RequestParam(required = false) String keyword,
                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                       @RequestParam(required = false) Boolean needsDistance,
                       @RequestParam(required = false) ReservationSort sort,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "10") int size,
                       Principal principal,
                       Model model) {
        LocalDate currentDate = LocalDate.now();
        long failedEmailCount = customerNotificationService.countFailedEmails();
        ReservationSummary summary = reservationService.summary();

        ReservationSort selectedSort = sort == null ? ReservationSort.PRIORITY : sort;
        int selectedSize = selectedPageSize(size);
        Page<Reservation> reservationPage = reservationService.searchPage(
                status,
                keyword,
                startDate,
                endDate,
                needsDistance,
                selectedSort,
                PageRequest.of(Math.max(page, 0), selectedSize)
        );
        List<Integer> pageNumbers = pageNumbers(reservationPage);
        String currentListUrl = currentListUrl(status, keyword, startDate, endDate, needsDistance, selectedSort, reservationPage.getNumber(), selectedSize);

        model.addAttribute("reservations", reservationPage.getContent());
        model.addAttribute("reservationPage", reservationPage);
        model.addAttribute("pageNumbers", pageNumbers);
        model.addAttribute("currentListUrl", currentListUrl);
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
        model.addAttribute("pageSizes", RESERVATION_PAGE_SIZES);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedSort", selectedSort);
        model.addAttribute("selectedSize", selectedSize);
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

    private List<Integer> pageNumbers(Page<?> page) {
        int totalPages = page.getTotalPages();

        if (totalPages == 0) {
            return List.of();
        }

        int startPage = Math.max(0, Math.min(page.getNumber() - 2, totalPages - 5));
        int endPage = Math.min(totalPages - 1, startPage + 4);

        return IntStream.rangeClosed(startPage, endPage)
                .boxed()
                .toList();
    }

    private int selectedPageSize(int size) {
        return RESERVATION_PAGE_SIZES.contains(size) ? size : DEFAULT_RESERVATION_PAGE_SIZE;
    }

    @GetMapping("/export.csv")
    public ResponseEntity<byte[]> exportCsv(@RequestParam(required = false) ReservationStatus status,
                                            @RequestParam(required = false) String keyword,
                                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                            @RequestParam(required = false) Boolean needsDistance,
                                            @RequestParam(required = false) ReservationSort sort,
                                            Principal principal) {
        ReservationSort selectedSort = sort == null ? ReservationSort.PRIORITY : sort;
        List<Reservation> reservations = reservationService.search(status, keyword, startDate, endDate, needsDistance, selectedSort);
        byte[] csv = adminReservationCsvExporter.export(reservations);

        adminAuditLogService.record(
                null,
                "예약 목록 CSV 다운로드",
                csvExportAuditDetail(status, keyword, startDate, endDate, needsDistance, selectedSort, reservations.size()),
                principal == null ? null : principal.getName()
        );

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename("admin-reservations.csv", StandardCharsets.UTF_8)
                        .build()
                        .toString())
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csv);
    }

    private String csvExportAuditDetail(ReservationStatus status,
                                        String keyword,
                                        LocalDate startDate,
                                        LocalDate endDate,
                                        Boolean needsDistance,
                                        ReservationSort sort,
                                        int exportedCount) {
        return "다운로드 건수 " + exportedCount + "건"
                + " / 상태 " + (status == null ? "전체" : status.getLabel())
                + " / 검색어 " + (keyword == null || keyword.isBlank() ? "없음" : keyword.trim())
                + " / 시작일 " + (startDate == null ? "전체" : startDate)
                + " / 종료일 " + (endDate == null ? "전체" : endDate)
                + " / 거리 확인 필요 " + (Boolean.TRUE.equals(needsDistance) ? "예" : "아니오")
                + " / 정렬 " + sort.getLabel();
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id,
                         @RequestParam(required = false) String returnQuery,
                         Model model) {
        Reservation reservation = reservationService.get(id);
        String backToListUrl = listRedirectUrl(returnQuery);

        model.addAttribute("reservation", reservation);
        model.addAttribute("returnQuery", backToListUrl);
        model.addAttribute("backToListUrl", backToListUrl);
        model.addAttribute("today", LocalDate.now());
        model.addAttribute("estimateLines", reservationService.estimateLines(reservation));
        model.addAttribute("photos", reservationService.findPhotos(id));
        model.addAttribute("statusHistories", reservationService.findStatusHistories(id));
        model.addAttribute("customerActionHistories", reservationService.findCustomerActionHistories(id));
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
                               @RequestParam(defaultValue = "detail") String returnTo,
                               @RequestParam(required = false) String returnQuery,
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
            redirectAttributes.addFlashAttribute("statusMessage",
                    "예약 " + id + "번 상태를 '" + status.getLabel() + "'(으)로 변경했습니다.");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("statusError", exception.getMessage());
        }

        if ("list".equals(returnTo)) {
            return "redirect:" + listRedirectUrl(returnQuery);
        }

        if ("calendar".equals(returnTo)) {
            return "redirect:" + calendarRedirectUrl(returnQuery);
        }

        return "redirect:" + detailRedirectUrl(id, returnQuery);
    }

    private String listRedirectUrl(String returnQuery) {
        if (returnQuery == null || returnQuery.isBlank()) {
            return "/admin/reservations";
        }

        try {
            URI uri = new URI(returnQuery);
            String path = uri.getPath();

            if (!"/admin/reservations".equals(path)) {
                return "/admin/reservations";
            }

            String query = uri.getRawQuery();
            return query == null || query.isBlank() ? path : path + "?" + query;
        } catch (URISyntaxException exception) {
            return "/admin/reservations";
        }
    }

    private String calendarRedirectUrl(String returnQuery) {
        if (returnQuery == null || returnQuery.isBlank()) {
            return "/admin/calendar";
        }

        try {
            URI uri = new URI(returnQuery);
            String path = uri.getPath();

            if (!"/admin/calendar".equals(path)) {
                return "/admin/calendar";
            }

            String query = uri.getRawQuery();
            return query == null || query.isBlank() ? path : path + "?" + query;
        } catch (URISyntaxException exception) {
            return "/admin/calendar";
        }
    }

    private String detailRedirectUrl(Long id, String returnQuery) {
        String listUrl = listRedirectUrl(returnQuery);

        if ("/admin/reservations".equals(listUrl)) {
            return "/admin/reservations/" + id;
        }

        return "/admin/reservations/" + id
                + "?returnQuery=" + UriUtils.encodeQueryParam(listUrl, StandardCharsets.UTF_8);
    }

    private String currentListUrl(ReservationStatus status,
                                  String keyword,
                                  LocalDate startDate,
                                  LocalDate endDate,
                                  Boolean needsDistance,
                                  ReservationSort sort,
                                  int page,
                                  int size) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromPath("/admin/reservations");

        if (status != null) {
            builder.queryParam("status", status);
        }

        if (keyword != null && !keyword.isBlank()) {
            builder.queryParam("keyword", keyword);
        }

        if (startDate != null) {
            builder.queryParam("startDate", startDate);
        }

        if (endDate != null) {
            builder.queryParam("endDate", endDate);
        }

        if (Boolean.TRUE.equals(needsDistance)) {
            builder.queryParam("needsDistance", true);
        }

        if (sort != null) {
            builder.queryParam("sort", sort);
        }

        if (page > 0) {
            builder.queryParam("page", page);
        }

        if (size != DEFAULT_RESERVATION_PAGE_SIZE) {
            builder.queryParam("size", size);
        }

        return builder.build().encode().toUriString();
    }

    @PostMapping("/{id}/estimate")
    public String updateEstimate(@PathVariable Long id,
                                 @RequestParam Integer estimatedPrice,
                                 @RequestParam(defaultValue = "detail") String returnTo,
                                 @RequestParam(required = false) String returnQuery,
                                 Principal principal,
                                 RedirectAttributes redirectAttributes) {
        Reservation reservation = reservationService.get(id);
        reservationService.updateEstimate(id, estimatedPrice);
        adminAuditLogService.record(
                reservation,
                "견적 금액 저장",
                "견적 금액을 " + String.format("%,d", estimatedPrice) + "원으로 저장했습니다.",
                principal.getName()
        );
        redirectAttributes.addFlashAttribute("estimateMessage",
                "예약 " + id + "번 견적을 " + String.format("%,d", estimatedPrice) + "원으로 저장했습니다.");

        if ("list".equals(returnTo)) {
            return "redirect:" + listRedirectUrl(returnQuery);
        }

        return "redirect:" + detailRedirectUrl(id, returnQuery);
    }

    @PostMapping("/{id}/distance")
    public String updateDistance(@PathVariable Long id,
                                 @RequestParam(required = false) Integer distanceKm,
                                 @RequestParam(defaultValue = "detail") String returnTo,
                                 @RequestParam(required = false) String returnQuery,
                                 Principal principal,
                                 RedirectAttributes redirectAttributes) {
        Reservation reservation = reservationService.get(id);
        reservationService.updateDistance(id, distanceKm);
        adminAuditLogService.record(
                reservation,
                "이동 거리 저장",
                distanceKm == null ? "이동 거리를 확인 전으로 저장했습니다." : "이동 거리를 " + distanceKm + "km로 저장했습니다.",
                principal.getName()
        );
        redirectAttributes.addFlashAttribute("distanceMessage",
                distanceKm == null
                        ? "예약 " + id + "번 이동 거리를 확인 전으로 저장했습니다."
                        : "예약 " + id + "번 이동 거리를 " + distanceKm + "km로 저장했습니다.");

        if ("list".equals(returnTo)) {
            return "redirect:" + listRedirectUrl(returnQuery);
        }

        return "redirect:" + detailRedirectUrl(id, returnQuery);
    }

    @PostMapping("/{id}/memo")
    public String updateAdminMemo(@PathVariable Long id,
                                  @RequestParam(required = false) String adminMemo,
                                  @RequestParam(required = false) String returnQuery,
                                  Principal principal) {
        Reservation reservation = reservationService.get(id);
        reservationService.updateAdminMemo(id, adminMemo, principal.getName());
        adminAuditLogService.record(
                reservation,
                "관리자 메모 저장",
                "관리자 메모를 저장했습니다.",
                principal.getName()
        );
        return "redirect:" + detailRedirectUrl(id, returnQuery);
    }

    @PostMapping("/{id}/notifications/email/send")
    public String sendReadyEmails(@PathVariable Long id,
                                  @RequestParam(required = false) String returnQuery,
                                  Principal principal,
                                  RedirectAttributes redirectAttributes) {
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

        return "redirect:" + detailRedirectUrl(id, returnQuery);
    }

    @PostMapping("/{id}/notifications/email/resend-failed")
    public String resendFailedEmails(@PathVariable Long id,
                                     @RequestParam(required = false) String returnQuery,
                                     Principal principal,
                                     RedirectAttributes redirectAttributes) {
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

        return "redirect:" + detailRedirectUrl(id, returnQuery);
    }
}
