package com.moving.reservation.reservation;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reservations")
public class ReservationApiController {

    private final ReservationService reservationService;

    public ReservationApiController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping("/search")
    public ResponseEntity<?> search(@Valid @RequestBody ReservationSearchRequest request,
                                    BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            String message = bindingResult.getFieldErrors().stream()
                    .findFirst()
                    .map(error -> error.getDefaultMessage())
                    .orElse("예약 조회 정보를 확인해 주세요.");
            return ResponseEntity.badRequest().body(new ReservationApiErrorResponse(message));
        }

        try {
            Reservation reservation = reservationService.search(request);
            return ResponseEntity.ok(ReservationApiResponse.from(
                    reservation,
                    reservationService.estimateLines(reservation)
            ));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ReservationApiErrorResponse(exception.getMessage()));
        }
    }
}
