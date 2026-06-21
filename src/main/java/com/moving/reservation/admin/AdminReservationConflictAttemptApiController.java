package com.moving.reservation.admin;

import com.moving.reservation.reservation.ReservationConflictAttemptService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/reservation-conflict-attempts")
public class AdminReservationConflictAttemptApiController {

    private final ReservationConflictAttemptService conflictAttemptService;

    public AdminReservationConflictAttemptApiController(ReservationConflictAttemptService conflictAttemptService) {
        this.conflictAttemptService = conflictAttemptService;
    }

    @GetMapping
    public List<AdminReservationConflictAttemptResponse> recent() {
        return conflictAttemptService.findRecent().stream()
                .map(AdminReservationConflictAttemptResponse::from)
                .toList();
    }
}
