package com.moving.reservation.reservation;

import com.moving.reservation.admin.EstimateDocumentPdfService;
import com.moving.reservation.review.ReviewService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationService reservationService;
    private final ReviewService reviewService;
    private final EstimateDocumentPdfService estimateDocumentPdfService;
    private final CustomerGuideService customerGuideService;

    public ReservationController(ReservationService reservationService,
                                 ReviewService reviewService,
                                 EstimateDocumentPdfService estimateDocumentPdfService,
                                 CustomerGuideService customerGuideService) {
        this.reservationService = reservationService;
        this.reviewService = reviewService;
        this.estimateDocumentPdfService = estimateDocumentPdfService;
        this.customerGuideService = customerGuideService;
    }

    @GetMapping("/new")
    public String newReservation(Model model) {
        model.addAttribute("reservationCreateRequest", new ReservationCreateRequest());
        model.addAttribute("moveTypes", MoveType.values());
        return "reservation/new";
    }

    @GetMapping("/estimate-preview")
    public ResponseEntity<?> estimatePreview(@RequestParam MoveType moveType,
                                             @RequestParam(defaultValue = "false") boolean fromElevator,
                                             @RequestParam(defaultValue = "false") boolean toElevator,
                                             @RequestParam(required = false) Integer fromFloor,
                                             @RequestParam(required = false) Integer toFloor,
                                             @RequestParam(defaultValue = "false") boolean fromLadderTruck,
                                             @RequestParam(defaultValue = "false") boolean toLadderTruck,
                                             @RequestParam(required = false) Integer distanceKm,
                                             @RequestParam(required = false) String couponCode) {
        try {
            return ResponseEntity.ok(reservationService.previewEstimate(
                    moveType,
                    fromElevator,
                    toElevator,
                    fromFloor,
                    toFloor,
                    fromLadderTruck,
                    toLadderTruck,
                    distanceKm,
                    couponCode
            ));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(exception.getMessage());
        }
    }

    @GetMapping("/search")
    public String searchForm(Model model) {
        if (!model.containsAttribute("reservationSearchRequest")) {
            model.addAttribute("reservationSearchRequest", new ReservationSearchRequest());
        }
        return "reservation/search";
    }

    @PostMapping("/search")
    public String search(@Valid @ModelAttribute ReservationSearchRequest request,
                         BindingResult bindingResult,
                         Model model,
                         HttpSession session) {
        if (bindingResult.hasErrors()) {
            return "reservation/search";
        }

        try {
            Reservation reservation = reservationService.search(request);
            ReservationAccessSession.authorize(session, reservation.getId());
            return "redirect:/reservations/" + reservation.getId();
        } catch (IllegalArgumentException exception) {
            model.addAttribute("searchError", exception.getMessage());
            return "reservation/search";
        }
    }

    @PostMapping
    public String create(@Valid @ModelAttribute ReservationCreateRequest request,
                         BindingResult bindingResult,
                         Model model,
                         HttpSession session) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("moveTypes", MoveType.values());
            return "reservation/new";
        }

        try {
            Reservation reservation = reservationService.create(request);
            ReservationAccessSession.authorize(session, reservation.getId());
            return "redirect:/reservations/" + reservation.getId();
        } catch (IllegalArgumentException | IllegalStateException | ReservationScheduleConflictException exception) {
            model.addAttribute("moveTypes", MoveType.values());
            model.addAttribute("uploadError", exception.getMessage());
            return "reservation/new";
        }
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id,
                         Model model,
                         HttpSession session,
                         Authentication authentication,
                         RedirectAttributes redirectAttributes) {
        if (!hasReservationAccess(id, session, authentication)) {
            redirectAttributes.addFlashAttribute("searchError", "예약 조회를 먼저 인증해 주세요.");
            return "redirect:/reservations/search";
        }

        Reservation reservation = reservationService.get(id);
        model.addAttribute("reservation", reservation);
        model.addAttribute("estimateLines", reservationService.estimateLines(reservation));
        model.addAttribute("photos", reservationService.findPhotos(id));
        model.addAttribute("customerActionHistories", reservationService.findCustomerActionHistories(id));
        model.addAttribute("review", reviewService.findByReservationId(id).orElse(null));
        model.addAttribute("customerDetailTitle", customerDetailTitle(reservation));
        model.addAttribute("customerDetailDescription", customerDetailDescription(reservation));
        model.addAttribute("customerSteps", customerSteps(reservation));
        model.addAttribute("customerNextGuide", customerNextGuide(reservation));
        model.addAttribute("customerGuideItems", customerGuideService.findActiveItems(reservation.getStatus()));
        return "reservation/detail";
    }

    @GetMapping("/{id}/estimate-document")
    public String estimateDocument(@PathVariable Long id,
                                   Model model,
                                   HttpSession session,
                                   Authentication authentication,
                                   RedirectAttributes redirectAttributes) {
        if (!hasReservationAccess(id, session, authentication)) {
            redirectAttributes.addFlashAttribute("searchError", "견적 확정서를 보려면 먼저 예약 조회 인증을 해주세요.");
            return "redirect:/reservations/search";
        }

        Reservation reservation = reservationService.get(id);
        model.addAttribute("reservation", reservation);
        model.addAttribute("estimateLines", reservationService.estimateLines(reservation));
        model.addAttribute("customerMode", true);
        return "admin/estimate-document";
    }

    @GetMapping("/{id}/estimate-document.pdf")
    public ResponseEntity<byte[]> estimateDocumentPdf(@PathVariable Long id,
                                                      HttpSession session,
                                                      Authentication authentication) {
        if (!hasReservationAccess(id, session, authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

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

    @PostMapping("/{id}/estimate/accept")
    public String acceptEstimate(@PathVariable Long id,
                                 RedirectAttributes redirectAttributes,
                                 HttpSession session,
                                 Authentication authentication) {
        if (!hasReservationAccess(id, session, authentication)) {
            redirectAttributes.addFlashAttribute("searchError", "견적 동의를 하려면 먼저 예약 조회 인증을 해주세요.");
            return "redirect:/reservations/search";
        }

        try {
            reservationService.acceptEstimate(id);
            redirectAttributes.addFlashAttribute("estimateAcceptMessage", "견적 동의가 완료되었습니다. 예약 상태가 확정으로 변경되었습니다.");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("estimateAcceptError", exception.getMessage());
        }

        return "redirect:/reservations/" + id;
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id,
                           Model model,
                           HttpSession session,
                           Authentication authentication,
                           RedirectAttributes redirectAttributes) {
        if (!hasReservationAccess(id, session, authentication)) {
            redirectAttributes.addFlashAttribute("searchError", "예약 수정을 하려면 먼저 예약 조회 인증을 해주세요.");
            return "redirect:/reservations/search";
        }

        Reservation reservation = reservationService.get(id);
        model.addAttribute("reservation", reservation);
        model.addAttribute("reservationUpdateRequest", ReservationUpdateRequest.from(reservation));
        return "reservation/edit";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute ReservationUpdateRequest request,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes,
                         HttpSession session,
                         Authentication authentication) {
        if (!hasReservationAccess(id, session, authentication)) {
            redirectAttributes.addFlashAttribute("searchError", "예약 수정을 하려면 먼저 예약 조회 인증을 해주세요.");
            return "redirect:/reservations/search";
        }

        Reservation reservation = reservationService.get(id);

        if (bindingResult.hasErrors()) {
            model.addAttribute("reservation", reservation);
            return "reservation/edit";
        }

        try {
            reservationService.updateDetails(id, request);
            redirectAttributes.addFlashAttribute("updateMessage", "예약 정보가 수정되었습니다.");
            return "redirect:/reservations/" + id;
        } catch (IllegalArgumentException exception) {
            model.addAttribute("reservation", reservation);
            model.addAttribute("updateError", exception.getMessage());
            return "reservation/edit";
        }
    }

    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable Long id,
                         @RequestParam String phone,
                         RedirectAttributes redirectAttributes,
                         HttpSession session,
                         Authentication authentication) {
        if (!hasReservationAccess(id, session, authentication)) {
            redirectAttributes.addFlashAttribute("searchError", "예약 취소를 하려면 먼저 예약 조회 인증을 해주세요.");
            return "redirect:/reservations/search";
        }

        try {
            reservationService.cancel(id, phone);
            redirectAttributes.addFlashAttribute("cancelMessage", "예약이 취소되었습니다.");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("cancelError", exception.getMessage());
        }

        return "redirect:/reservations/" + id;
    }

    private boolean hasReservationAccess(Long reservationId, HttpSession session, Authentication authentication) {
        return ReservationAccessSession.isAuthorized(session, reservationId) || isAdmin(authentication);
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
    }

    private List<CustomerReservationStep> customerSteps(Reservation reservation) {
        List<ReservationStatus> flow = List.of(
                ReservationStatus.RECEIVED,
                ReservationStatus.CONSULTING,
                ReservationStatus.ESTIMATE_SENT,
                ReservationStatus.CONFIRMED,
                ReservationStatus.COMPLETED
        );
        int currentIndex = flow.indexOf(reservation.getStatus());

        return flow.stream()
                .map(status -> {
                    int stepIndex = flow.indexOf(status);
                    return new CustomerReservationStep(
                            status,
                            status.getLabel(),
                            customerStepDescription(status),
                            currentIndex >= 0 && stepIndex < currentIndex,
                            status == reservation.getStatus()
                    );
                })
                .toList();
    }

    private String customerStepDescription(ReservationStatus status) {
        return switch (status) {
            case RECEIVED -> "예약이 접수되었습니다.";
            case CONSULTING -> "일정과 현장 조건을 확인합니다.";
            case ESTIMATE_SENT -> "확정 전 견적을 안내합니다.";
            case CONFIRMED -> "견적 동의 후 일정이 확정됩니다.";
            case COMPLETED -> "이사가 완료되었습니다.";
            case CANCELED -> "예약이 취소되었습니다.";
        };
    }

    private String customerNextGuide(Reservation reservation) {
        return switch (reservation.getStatus()) {
            case RECEIVED -> "예약 내용을 확인한 뒤 상담을 시작합니다. 연락을 기다려 주세요.";
            case CONSULTING -> "상담을 통해 주소, 짐 양, 현장 조건을 확인하고 견적을 안내합니다.";
            case ESTIMATE_SENT -> reservation.hasEstimateAcceptance()
                    ? "견적 동의가 완료되었습니다. 일정 확정을 기다려 주세요."
                    : "견적을 확인한 뒤 동의하면 예약이 확정됩니다.";
            case CONFIRMED -> "이사 일정이 확정되었습니다. 예약 내용을 다시 확인해 주세요.";
            case COMPLETED -> "이사가 완료되었습니다. 이용 후 리뷰를 남길 수 있습니다.";
            case CANCELED -> "예약이 취소되었습니다. 새 예약이 필요하면 예약 신청을 다시 진행해 주세요.";
        };
    }

    private String customerDetailTitle(Reservation reservation) {
        return switch (reservation.getStatus()) {
            case RECEIVED -> "예약이 접수되었습니다";
            case CONSULTING -> "상담이 진행 중입니다";
            case ESTIMATE_SENT -> "견적 안내를 확인해 주세요";
            case CONFIRMED -> "예약이 확정되었습니다";
            case COMPLETED -> "이사가 완료되었습니다";
            case CANCELED -> "예약이 취소되었습니다";
        };
    }

    private String customerDetailDescription(Reservation reservation) {
        String lookupNotice = " 예약 번호 "
                + reservation.getId()
                + "번은 나중에 예약 번호와 예약 당시 연락처로 다시 조회할 수 있습니다.";

        return switch (reservation.getStatus()) {
            case RECEIVED -> "예약 내용을 확인한 뒤 상담을 시작합니다." + lookupNotice;
            case CONSULTING -> "상담원이 주소, 짐 양, 현장 조건을 확인하고 있습니다." + lookupNotice;
            case ESTIMATE_SENT -> reservation.hasEstimateAcceptance()
                    ? "견적 동의가 완료되었습니다. 일정 확정을 기다려 주세요." + lookupNotice
                    : "최종 견적 금액과 산정 내역을 확인한 뒤 동의하면 예약이 확정됩니다." + lookupNotice;
            case CONFIRMED -> "이사 일정이 확정되었습니다. 이사 전 준비 사항을 확인해 주세요." + lookupNotice;
            case COMPLETED -> "이사가 완료되었습니다. 이용 후 리뷰를 남길 수 있습니다." + lookupNotice;
            case CANCELED -> "취소된 예약은 조회용으로만 확인할 수 있습니다. 새 이사가 필요하면 다시 예약을 신청해 주세요.";
        };
    }

}
