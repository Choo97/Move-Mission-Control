package com.moving.reservation.admin;

import com.moving.reservation.reservation.Reservation;
import com.moving.reservation.reservation.ReservationService;
import com.moving.reservation.reservation.ReservationSort;
import com.moving.reservation.reservation.ReservationStatus;
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

    public AdminReservationApiController(ReservationService reservationService) {
        this.reservationService = reservationService;
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

        return AdminReservationDetailResponse.from(
                reservation,
                reservationService.estimateLines(reservation),
                reservationService.findPhotos(id),
                reservationService.findStatusHistories(id),
                reservationService.findCustomerActionHistories(id)
        );
    }

    @PatchMapping("/{id}/status")
    public AdminReservationDetailResponse updateStatus(@PathVariable Long id,
                                                       @Valid @RequestBody AdminReservationStatusUpdateRequest request,
                                                       Principal principal) {
        reservationService.updateStatus(id, request.getStatus(), principal.getName());
        Reservation reservation = reservationService.get(id);

        return AdminReservationDetailResponse.from(
                reservation,
                reservationService.estimateLines(reservation),
                reservationService.findPhotos(id),
                reservationService.findStatusHistories(id),
                reservationService.findCustomerActionHistories(id)
        );
    }

    private int selectedPageSize(int size) {
        return ALLOWED_PAGE_SIZES.contains(size) ? size : DEFAULT_PAGE_SIZE;
    }
}
