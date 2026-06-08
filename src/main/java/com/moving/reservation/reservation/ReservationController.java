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

    public ReservationController(ReservationService reservationService,
                                 ReviewService reviewService,
                                 EstimateDocumentPdfService estimateDocumentPdfService) {
        this.reservationService = reservationService;
        this.reviewService = reviewService;
        this.estimateDocumentPdfService = estimateDocumentPdfService;
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
        } catch (IllegalArgumentException | IllegalStateException exception) {
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
        model.addAttribute("review", reviewService.findByReservationId(id).orElse(null));
        model.addAttribute("customerSteps", customerSteps(reservation));
        model.addAttribute("customerNextGuide", customerNextGuide(reservation));
        model.addAttribute("customerGuideItems", customerGuideItems(reservation));
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

    private List<CustomerGuideItem> customerGuideItems(Reservation reservation) {
        return switch (reservation.getStatus()) {
            case RECEIVED -> List.of(
                    new CustomerGuideItem("연락 받을 준비", "상담 전화나 안내 메일을 확인할 수 있도록 연락처와 이메일을 확인해 주세요."),
                    new CustomerGuideItem("예약 정보 확인", "이사일, 출발지, 도착지, 층수 정보가 맞는지 다시 확인해 주세요."),
                    new CustomerGuideItem("짐 사진 준비", "짐 사진을 올렸다면 상담 때 더 빠르게 견적을 안내받을 수 있습니다.")
            );
            case CONSULTING -> List.of(
                    new CustomerGuideItem("현장 조건 확인", "엘리베이터 사용 가능 여부, 주차 위치, 사다리차 필요 여부를 확인해 주세요."),
                    new CustomerGuideItem("짐 양 설명", "큰 가구, 가전, 분해가 필요한 물건이 있으면 상담 때 알려 주세요."),
                    new CustomerGuideItem("견적 안내 대기", "상담 내용이 정리되면 최종 견적을 안내해 드립니다.")
            );
            case ESTIMATE_SENT -> List.of(
                    new CustomerGuideItem("최종 견적 확인", "안내된 최종 견적 금액과 산정 내역을 확인해 주세요."),
                    new CustomerGuideItem("견적 동의 진행", "금액이 맞으면 견적 동의 버튼으로 예약을 확정할 수 있습니다."),
                    new CustomerGuideItem("변경 사항 문의", "주소, 날짜, 짐 양이 바뀌면 동의 전 문의해 주세요.")
            );
            case CONFIRMED -> List.of(
                    new CustomerGuideItem("이사 전 정리", "파손 위험 물건과 귀중품은 따로 분류해 두면 당일 작업이 빨라집니다."),
                    new CustomerGuideItem("출입 동선 확인", "주차 공간, 공동현관, 엘리베이터 사용 시간을 미리 확인해 주세요."),
                    new CustomerGuideItem("당일 연락 확인", "이사 당일 연락 가능한 휴대폰을 확인하고 대기해 주세요.")
            );
            case COMPLETED -> List.of(
                    new CustomerGuideItem("리뷰 작성", "서비스 이용 후 느낀 점을 남기면 운영 품질 개선에 도움이 됩니다."),
                    new CustomerGuideItem("예약 내역 보관", "견적 확정서와 예약 정보를 필요할 때 다시 확인할 수 있습니다."),
                    new CustomerGuideItem("추가 문의", "추가 정리나 문의가 있으면 예약 정보를 기준으로 다시 상담할 수 있습니다.")
            );
            case CANCELED -> List.of(
                    new CustomerGuideItem("취소 내역 확인", "취소된 예약은 현재 상태와 예약 정보를 조회용으로만 확인할 수 있습니다."),
                    new CustomerGuideItem("새 예약 신청", "이사가 다시 필요하면 메인 화면에서 새 예약을 신청해 주세요."),
                    new CustomerGuideItem("문의 필요 시 연락", "취소 사유나 재예약 조건을 확인해야 하면 관리자에게 문의해 주세요.")
            );
        };
    }
}
