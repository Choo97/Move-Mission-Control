package com.moving.reservation.reservation;

import com.moving.reservation.admin.EstimateDocumentPdfService;
import com.moving.reservation.review.ReviewService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
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
}
