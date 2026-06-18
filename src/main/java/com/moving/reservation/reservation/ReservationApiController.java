package com.moving.reservation.reservation;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> create(@Valid @RequestBody ReservationApiCreateRequest request,
                                    BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(new ReservationApiErrorResponse(firstErrorMessage(bindingResult)));
        }

        try {
            Reservation reservation = reservationService.create(request.toServiceRequest());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ReservationApiResponse.from(
                            reservation,
                            reservationService.estimateLines(reservation)
                    ));
        } catch (IllegalArgumentException | IllegalStateException exception) {
            return ResponseEntity.badRequest().body(new ReservationApiErrorResponse(exception.getMessage()));
        }
    }

    @PostMapping("/search")
    public ResponseEntity<?> search(@Valid @RequestBody ReservationSearchRequest request,
                                    BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(new ReservationApiErrorResponse(firstErrorMessage(bindingResult)));
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

    @PatchMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> update(@PathVariable Long id,
                                    @Valid @RequestBody ReservationUpdateRequest request,
                                    BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(new ReservationApiErrorResponse(firstErrorMessage(bindingResult)));
        }

        try {
            reservationService.updateDetails(id, request);
            Reservation reservation = reservationService.get(id);
            return ResponseEntity.ok(ReservationApiResponse.from(
                    reservation,
                    reservationService.estimateLines(reservation)
            ));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(new ReservationApiErrorResponse(exception.getMessage()));
        }
    }

    @PostMapping(value = "/{id}/cancel", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> cancel(@PathVariable Long id,
                                    @Valid @RequestBody ReservationApiCancelRequest request,
                                    BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(new ReservationApiErrorResponse(firstErrorMessage(bindingResult)));
        }

        try {
            reservationService.cancel(id, request.getPhone());
            Reservation reservation = reservationService.get(id);
            return ResponseEntity.ok(ReservationApiResponse.from(
                    reservation,
                    reservationService.estimateLines(reservation)
            ));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(new ReservationApiErrorResponse(exception.getMessage()));
        }
    }

    @PostMapping(value = "/{id}/estimate/accept", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> acceptEstimate(@PathVariable Long id,
                                            @Valid @RequestBody ReservationApiEstimateAcceptRequest request,
                                            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(new ReservationApiErrorResponse(firstErrorMessage(bindingResult)));
        }

        try {
            reservationService.acceptEstimate(id, request.getPhone());
            Reservation reservation = reservationService.get(id);
            return ResponseEntity.ok(ReservationApiResponse.from(
                    reservation,
                    reservationService.estimateLines(reservation)
            ));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(new ReservationApiErrorResponse(exception.getMessage()));
        }
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ReservationApiErrorResponse> handleIllegalArgument(IllegalArgumentException exception) {
        return ResponseEntity.badRequest().body(new ReservationApiErrorResponse(exception.getMessage()));
    }

    private String firstErrorMessage(BindingResult bindingResult) {
        return bindingResult.getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse("요청 정보를 확인해 주세요.");
    }
}
